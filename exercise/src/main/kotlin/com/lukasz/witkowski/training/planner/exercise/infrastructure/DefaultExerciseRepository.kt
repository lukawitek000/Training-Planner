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
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepositoryConfig
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
    private val config: ExerciseRepositoryConfig = ExerciseRepositoryConfig(),
    private val ioDispatcher: CoroutineDispatcher,
) : ExerciseRepository {

    override fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails?> =
        localDataSource.getExerciseDetailsById(id.toString()).map {
            it?.toExerciseDetails()
        }

    override fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>> = Pager(
        config = PagingConfig(
            pageSize = config.pagingConfig.pageSize,
            prefetchDistance = config.pagingConfig.prefetchDistance
        ),
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
        localDataSource.insertExerciseDetails(dbExerciseDetails)
    }

    override suspend fun delete(exerciseId: ExerciseId): Boolean = withContext(ioDispatcher) {
        localDataSource.deleteExerciseById(exerciseId.toString())
    }

    override suspend fun updateExercise(exercise: ExerciseDetails): Boolean =
        withContext(ioDispatcher) {
            val idempotencyKey = UUID.randomUUID().toString()
            val mutation = exercise.toPendingMutation(idempotencyKey)
            val updatedExerciseDetails = exercise.toPendingUploadDetails()

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
        val dbExerciseDetails = serverExerciseDetails.toDbExerciseDetails()
        localDataSource.insertExerciseDetails(dbExerciseDetails)
        localDataSource.deleteMutationsForExercise(exerciseId.toString())
        true
    }

    private fun enqueueMutationWorker() {
        mutationScheduler.enqueueMutationSync()
    }
}

private fun ExerciseDetails.toPendingMutation(idempotencyKey: String): DbExerciseMutation =
    DbExerciseMutation(
        id = UUID.randomUUID().toString(),
        exerciseId = exercise.id.toString(),
        idempotencyKey = idempotencyKey,
        expectedVersion = exercise.metadata.revision,
        name = exercise.name,
        description = exercise.description,
        categoryNames = exercise.categories.joinToString(",") { it.name },
        state = "PENDING",
        beginnerSets = beginnerRecommendation.sets,
        beginnerReps = beginnerRecommendation.reps,
        beginnerRestTime = beginnerRecommendation.restTime.inWholeMilliseconds,
        beginnerWeight = beginnerRecommendation.weightInKg,
        intermediateSets = intermediateRecommendation.sets,
        intermediateReps = intermediateRecommendation.reps,
        intermediateRestTime = intermediateRecommendation.restTime.inWholeMilliseconds,
        intermediateWeight = intermediateRecommendation.weightInKg,
        advancedSets = advancedRecommendation.sets,
        advancedReps = advancedRecommendation.reps,
        advancedRestTime = advancedRecommendation.restTime.inWholeMilliseconds,
        advancedWeight = advancedRecommendation.weightInKg
    )

private fun ExerciseDetails.toPendingUploadDetails(): ExerciseDetails = copy(
    exercise = exercise.copy(
        metadata = exercise.metadata.copy(syncState = ExerciseSyncState.PENDING_UPLOAD)
    )
)

private fun DbExerciseMutation.toUpdatedPendingMutation(latestServerVersion: Long): DbExerciseMutation =
    copy(
        expectedVersion = latestServerVersion,
        state = "PENDING"
    )

private fun DbExerciseDetails.toPendingUploadDbExerciseDetails(latestServerVersion: Long): DbExerciseDetails {
    val updatedDbEx = DbExercise(
        exerciseId = exercise.exerciseId,
        name = exercise.name,
        description = exercise.description,
        imageId = exercise.imageId,
        owner = exercise.owner,
        sourceType = exercise.sourceType,
        sharing = exercise.sharing,
        revision = latestServerVersion,
        syncState = DbExercise.SYNC_STATE_PENDING_UPLOAD
    )
    return copy(exercise = updatedDbEx)
}
