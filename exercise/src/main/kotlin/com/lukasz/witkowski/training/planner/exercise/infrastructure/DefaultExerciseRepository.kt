package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMutationScheduler
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.ExerciseLocalDataSource
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class DefaultExerciseRepository(
    private val localDataSource: ExerciseLocalDataSource,
    private val mutationScheduler: ExerciseMutationScheduler,
    private val ioDispatcher: CoroutineDispatcher,
    private val pagingConfig: PagingConfig = PagingConfig(
        pageSize = DEFAULT_PAGE_SIZE,
        prefetchDistance = DEFAULT_PREFETCH_DISTANCE
    ),
) : ExerciseRepository {

    override fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails?> =
        localDataSource.getExerciseDetailsById(id.toString()).map {
            it?.toExerciseDetails()
        }

    override fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = {
            localDataSource.getExercisesWithCategories(
                query = query.query,
                categoriesNames = query.categories.map { it.name }
            )
        }
    ).flow.map { pagingData ->
        pagingData.map { dbExercise ->
            Timber.d("Fetched exercise: ${dbExercise.exercise.name}")
            dbExercise.toExercise()
        }
    }

    override suspend fun insert(exercise: ExerciseDetails): Boolean = withContext(ioDispatcher) {
        val dbExerciseDetails = exercise.toDbExerciseDetails()
        val inserted = localDataSource.insertExerciseDetails(dbExerciseDetails)
        if (inserted && (
                exercise.exercise.metadata.source is com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource.UserCreated ||
                    exercise.exercise.metadata.syncState == ExerciseSyncState.PENDING_UPLOAD
                )
        ) {
            val idempotencyKey = UUID.randomUUID().toString()
            val mutation = exercise.toPendingMutation(
                idempotencyKey = idempotencyKey,
                type = DbExerciseMutation.TYPE_CREATE
            )
            localDataSource.insertMutation(mutation)
            enqueueMutationWorker()
        }
        inserted
    }

    override suspend fun delete(exerciseId: ExerciseId): Boolean = withContext(ioDispatcher) {
        val existingDbDetails = localDataSource.getExerciseDetailsById(exerciseId.toString()).firstOrNull()
            ?: return@withContext false
        val existingMutation = localDataSource.getMutationByExerciseIdOnce(exerciseId.toString())
        val isUnsyncedLocal = existingMutation?.type == DbExerciseMutation.TYPE_CREATE ||
            existingDbDetails.exercise.syncState == DbExercise.SYNC_STATE_LOCAL_ONLY ||
            existingDbDetails.exercise.owner == null ||
            existingDbDetails.exercise.owner.ownerName == "Local"

        if (isUnsyncedLocal) {
            localDataSource.deleteExerciseById(exerciseId.toString())
        } else {
            val existingDomain = existingDbDetails.toExerciseDetails()
            val deleteMutation = existingDomain.toPendingMutation(
                idempotencyKey = UUID.randomUUID().toString(),
                type = DbExerciseMutation.TYPE_DELETE
            )
            localDataSource.updateSyncState(exerciseId.toString(), DbExercise.SYNC_STATE_PENDING_DELETE)
            localDataSource.insertMutation(deleteMutation)
            enqueueMutationWorker()
            true
        }
    }

    override suspend fun updateExercise(exercise: ExerciseDetails): Boolean =
        withContext(ioDispatcher) {
            val existingDbDetails = localDataSource.getExerciseDetailsById(exercise.exercise.id.toString()).firstOrNull()
                ?: return@withContext false
            val existingMutation = localDataSource.getMutationByExerciseIdOnce(exercise.exercise.id.toString())

            val mutationType = if (existingMutation?.type == DbExerciseMutation.TYPE_CREATE) {
                DbExerciseMutation.TYPE_CREATE
            } else {
                DbExerciseMutation.TYPE_UPDATE
            }
            val idempotencyKey = if (mutationType == DbExerciseMutation.TYPE_CREATE && existingMutation != null) {
                existingMutation.idempotencyKey
            } else {
                UUID.randomUUID().toString()
            }
            val mutation = exercise.toPendingMutation(
                idempotencyKey = idempotencyKey,
                type = mutationType
            )
            val shouldMarkPendingUpload =
                exercise.exercise.metadata.source is com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource.UserCreated ||
                    existingDbDetails.exercise.sourceType == DbExercise.SOURCE_TYPE_USER_CREATED ||
                    exercise.exercise.metadata.syncState == ExerciseSyncState.PENDING_UPLOAD

            val updatedExerciseDetails = if (shouldMarkPendingUpload) {
                exercise.toPendingUploadDetails()
            } else {
                exercise
            }

            localDataSource.insertExerciseDetails(updatedExerciseDetails.toDbExerciseDetails())
            localDataSource.insertMutation(mutation)
            enqueueMutationWorker()
            true
        }

    override suspend fun saveExerciseLocally(exerciseDetails: ExerciseDetails): Boolean =
        withContext(ioDispatcher) {
            val dbExerciseDetails = exerciseDetails.toDbExerciseDetails()
            localDataSource.insertExerciseDetails(dbExerciseDetails)
        }

    override suspend fun resolveConflictKeepLocal(
        exerciseId: ExerciseId,
        latestServerVersion: Long
    ): Boolean = withContext(ioDispatcher) {
        val existingMutation = localDataSource.getMutationByExerciseIdOnce(exerciseId.toString())
        if (existingMutation != null) {
            val updatedMutation = existingMutation.toUpdatedPendingMutation(latestServerVersion)
            localDataSource.updateMutation(updatedMutation)

            val dbDetails = localDataSource.getExerciseDetailsById(exerciseId.toString()).firstOrNull()
            if (dbDetails != null) {
                val updatedDbDetails = dbDetails.toPendingUploadDbExerciseDetails(latestServerVersion)
                localDataSource.insertExerciseDetails(updatedDbDetails)
            }
            enqueueMutationWorker()
            true
        } else {
            false
        }
    }

    override suspend fun resolveConflictAcceptServer(
        exerciseId: ExerciseId,
        serverExerciseDetails: ExerciseDetails
    ): Boolean = withContext(ioDispatcher) {
        val existingDbDetails = localDataSource.getExerciseDetailsById(exerciseId.toString()).firstOrNull()
        val preservedImageId = existingDbDetails?.exercise?.imageId?.let {
            com.lukasz.witkowski.training.planner.image.ImageId(it)
        }
        val mergedServerDetails = if (serverExerciseDetails.exercise.imageId == null && preservedImageId != null) {
            serverExerciseDetails.copy(
                exercise = serverExerciseDetails.exercise.copy(imageId = preservedImageId)
            )
        } else {
            serverExerciseDetails
        }
        val dbExerciseDetails = mergedServerDetails.toDbExerciseDetails()
        localDataSource.insertExerciseDetails(dbExerciseDetails)
        localDataSource.deleteMutationsForExercise(exerciseId.toString())
        true
    }

    private fun enqueueMutationWorker() {
        mutationScheduler.enqueueMutationSync()
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PREFETCH_DISTANCE = 10
    }
}

private fun ExerciseDetails.toPendingMutation(
    idempotencyKey: String,
    type: String = DbExerciseMutation.TYPE_UPDATE
): DbExerciseMutation =
    DbExerciseMutation(
        id = UUID.randomUUID().toString(),
        exerciseId = exercise.id.toString(),
        idempotencyKey = idempotencyKey,
        expectedVersion = exercise.metadata.revision,
        name = exercise.name,
        description = exercise.description,
        categoryNames = exercise.categories.joinToString(",") { it.name },
        state = DbExerciseMutation.STATE_PENDING,
        type = type,
        beginnerSets = beginnerRecommendation.sets,
        beginnerReps = beginnerRecommendation.reps,
        beginnerRestTime = beginnerRecommendation.restTime.inWholeMilliseconds,
        beginnerWeight = beginnerRecommendation.weightInKg,
        beginnerTime = beginnerRecommendation.time?.inWholeMilliseconds,
        intermediateSets = intermediateRecommendation.sets,
        intermediateReps = intermediateRecommendation.reps,
        intermediateRestTime = intermediateRecommendation.restTime.inWholeMilliseconds,
        intermediateWeight = intermediateRecommendation.weightInKg,
        intermediateTime = intermediateRecommendation.time?.inWholeMilliseconds,
        advancedSets = advancedRecommendation.sets,
        advancedReps = advancedRecommendation.reps,
        advancedRestTime = advancedRecommendation.restTime.inWholeMilliseconds,
        advancedWeight = advancedRecommendation.weightInKg,
        advancedTime = advancedRecommendation.time?.inWholeMilliseconds
    )

private fun ExerciseDetails.toPendingUploadDetails(): ExerciseDetails = copy(
    exercise = exercise.copy(
        metadata = exercise.metadata.copy(syncState = ExerciseSyncState.PENDING_UPLOAD)
    )
)

private fun DbExerciseMutation.toUpdatedPendingMutation(latestServerVersion: Long): DbExerciseMutation =
    copy(
        idempotencyKey = UUID.randomUUID().toString(),
        expectedVersion = latestServerVersion,
        state = DbExerciseMutation.STATE_PENDING,
        conflictServerJson = null
    )

private fun DbExerciseDetails.toPendingUploadDbExerciseDetails(latestServerVersion: Long): DbExerciseDetails {
    val updatedDbEx = exercise.copy(
        revision = latestServerVersion,
        syncState = DbExercise.SYNC_STATE_PENDING_UPLOAD
    )
    return copy(exercise = updatedDbEx)
}
