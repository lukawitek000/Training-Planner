package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.exercise.domain.isRetryable
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExercise
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseMutation
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseMutationDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.toExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.infrastructure.toDbExerciseDetails
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber

internal class DefaultExerciseSyncRepository(
    private val exerciseDao: ExerciseDao,
    private val mutationDao: ExerciseMutationDao,
    private val remoteDataSource: ExerciseRemoteDataSource,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) : ExerciseSyncRepository {

    override suspend fun synchronizeExercises(): AppResult<Unit, ExerciseFailure> =
        withContext(ioDispatcher) {
            val currentVersion = exerciseDao.getSyncVersion() ?: 0L
            Timber.d("Starting exercise sync from version $currentVersion")
            when (val result = remoteDataSource.getExerciseChanges(currentVersion)) {
                is AppResult.Success -> {
                    val syncResponse = result.value
                    val upsertDbItems = syncResponse.changes.map { dto ->
                        dto.toExerciseDetails().toDbExerciseDetails()
                    }
                    exerciseDao.applySyncChanges(
                        upserts = upsertDbItems,
                        deletedIds = syncResponse.deletedIds,
                        newVersion = syncResponse.currentVersion
                    )
                    Timber.d(
                        "Exercise sync succeeded. Local sync version updated to %d",
                        syncResponse.currentVersion
                    )
                    AppResult.Success(Unit)
                }

                is AppResult.Error -> {
                    Timber.w("Exercise sync failed with error: %s", result.error)
                    AppResult.Error(result.error)
                }
            }
        }

    override suspend fun processPendingMutations(): AppResult<Unit, ExerciseFailure> =
        withContext(ioDispatcher) {
            val pendingMutations = mutationDao.getPendingMutations()
            if (pendingMutations.isEmpty()) {
                return@withContext AppResult.Success(Unit)
            }

            var anyFailed = false
            var lastError: ExerciseFailure = ExerciseFailure.UnknownFailure

            for (mutation in pendingMutations) {
                val result = syncMutation(mutation)
                if (result is AppResult.Error) {
                    if (!anyFailed || result.error.isRetryable() || !lastError.isRetryable()) {
                        lastError = result.error
                    }
                    anyFailed = true
                }
            }

            if (anyFailed) AppResult.Error(lastError) else AppResult.Success(Unit)
        }

    override suspend fun getConflictServerExercise(
        exerciseId: ExerciseId
    ): AppResult<ExerciseDetails, ExerciseFailure> = withContext(ioDispatcher) {
        val mutation = mutationDao.getMutationByExerciseIdOnce(exerciseId.toString())
        val cachedJson = mutation?.conflictServerJson
        if (!cachedJson.isNullOrBlank()) {
            try {
                val dto = json.decodeFromString<ExerciseDto>(cachedJson)
                return@withContext AppResult.Success(dto.toExerciseDetails())
            } catch (e: Exception) {
                Timber.w(e, "Failed to decode cached conflictServerJson for %s", exerciseId)
            }
        }
        remoteDataSource.getExerciseDetailsById(exerciseId)
    }

    override suspend fun resolveConflictKeepLocal(
        exerciseId: ExerciseId
    ): AppResult<Unit, ExerciseFailure> = withContext(ioDispatcher) {
        val serverResult = getConflictServerExercise(exerciseId)
        val serverRevision = when (serverResult) {
            is AppResult.Success -> serverResult.value.exercise.metadata.revision
            is AppResult.Error -> return@withContext AppResult.Error(serverResult.error)
        }
        val existingMutation = mutationDao.getMutationByExerciseIdOnce(exerciseId.toString())
        if (existingMutation != null) {
            mutationDao.updateMutation(
                existingMutation.copy(
                    state = DbExerciseMutation.STATE_PENDING,
                    expectedVersion = serverRevision,
                    idempotencyKey = java.util.UUID.randomUUID().toString(),
                    conflictServerJson = null
                )
            )
        }
        exerciseDao.updateSyncState(exerciseId.toString(), DbExercise.SYNC_STATE_PENDING_UPLOAD)
        AppResult.Success(Unit)
    }

    override suspend fun resolveConflictAcceptServer(
        exerciseId: ExerciseId
    ): AppResult<Unit, ExerciseFailure> = withContext(ioDispatcher) {
        val serverResult = getConflictServerExercise(exerciseId)
        val serverDetails = when (serverResult) {
            is AppResult.Success -> serverResult.value
            is AppResult.Error -> return@withContext AppResult.Error(serverResult.error)
        }
        val existingDbExercise = exerciseDao.getExerciseByIdOnce(exerciseId.toString())
        val existingImageId = existingDbExercise?.imageId?.takeIf {
            it.isNotBlank()
        }?.let { ImageId(it) }
        val detailsToSave = if (existingImageId != null && serverDetails.exercise.imageId == null) {
            serverDetails.copy(exercise = serverDetails.exercise.copy(imageId = existingImageId))
        } else {
            serverDetails
        }
        exerciseDao.insertExerciseDetails(detailsToSave.toDbExerciseDetails())
        mutationDao.deleteMutationsForExercise(exerciseId.toString())
        AppResult.Success(Unit)
    }

    override suspend fun onUserLogout(keepLocalData: Boolean) = withContext(ioDispatcher) {
        mutationDao.deleteAllMutations()
        exerciseDao.deleteSyncVersion()
        if (keepLocalData) {
            exerciseDao.deletePendingDeleteExercises()
            exerciseDao.convertNonBundledExercisesToLocalOnly()
        } else {
            exerciseDao.deleteAllNonBundledExercises()
        }
    }

    private suspend fun syncMutation(
        mutation: DbExerciseMutation
    ): AppResult<Unit, ExerciseFailure> {
        mutationDao.updateMutation(mutation.copy(state = DbExerciseMutation.STATE_IN_PROGRESS))

        if (mutation.type == DbExerciseMutation.TYPE_DELETE) {
            return when (
                val deleteResult = remoteDataSource.deleteExercise(
                    ExerciseId(mutation.exerciseId)
                )
            ) {
                is AppResult.Success -> {
                    exerciseDao.deleteExerciseAndCascade(mutation.exerciseId)
                    mutationDao.deleteMutation(mutation.id)
                    AppResult.Success(Unit)
                }
                is AppResult.Error -> {
                    val failure = deleteResult.error
                    if (failure is ExerciseFailure.ExerciseNotFound) {
                        exerciseDao.deleteExerciseAndCascade(mutation.exerciseId)
                        mutationDao.deleteMutation(mutation.id)
                        AppResult.Success(Unit)
                    } else {
                        val nextState = if (failure.isRetryable()) {
                            DbExerciseMutation.STATE_PENDING
                        } else {
                            DbExerciseMutation.STATE_ERROR
                        }
                        mutationDao.updateMutation(mutation.copy(state = nextState))
                        exerciseDao.updateSyncState(
                            mutation.exerciseId,
                            DbExercise.SYNC_STATE_ERROR
                        )
                        AppResult.Error(failure)
                    }
                }
            }
        }

        val existingDbExercise = exerciseDao.getExerciseByIdOnce(mutation.exerciseId)
        val existingImageId = existingDbExercise?.imageId?.takeIf {
            it.isNotBlank()
        }?.let { ImageId(it) }
        val categories = mutation.categoryNames
            .split(",")
            .filter { it.isNotBlank() }
            .map { ExerciseCategory(it) }
            .toSet()
            .ifEmpty { setOf(ExerciseCategory("Other")) }

        val domainExercise = Exercise2(
            id = ExerciseId(mutation.exerciseId),
            name = mutation.name,
            description = mutation.description,
            categories = categories,
            imageId = existingImageId,
            metadata = ExerciseMetadata(
                revision = mutation.expectedVersion,
                syncState = ExerciseSyncState.PENDING_UPLOAD
            )
        )

        val beginnerRec = ExerciseRecommendation(
            sets = mutation.beginnerSets ?: 0,
            reps = mutation.beginnerReps ?: 0,
            restTime = (mutation.beginnerRestTime ?: 0L).milliseconds,
            weightInKg = mutation.beginnerWeight,
            time = mutation.beginnerTime?.milliseconds
        )
        val intermediateRec = ExerciseRecommendation(
            sets = mutation.intermediateSets ?: 0,
            reps = mutation.intermediateReps ?: 0,
            restTime = (mutation.intermediateRestTime ?: 0L).milliseconds,
            weightInKg = mutation.intermediateWeight,
            time = mutation.intermediateTime?.milliseconds
        )
        val advancedRec = ExerciseRecommendation(
            sets = mutation.advancedSets ?: 0,
            reps = mutation.advancedReps ?: 0,
            restTime = (mutation.advancedRestTime ?: 0L).milliseconds,
            weightInKg = mutation.advancedWeight,
            time = mutation.advancedTime?.milliseconds
        )

        val exerciseDetails = ExerciseDetails(
            exercise = domainExercise,
            beginnerRecommendation = beginnerRec,
            intermediateRecommendation = intermediateRec,
            advancedRecommendation = advancedRec
        )

        val result = if (mutation.type == DbExerciseMutation.TYPE_CREATE) {
            remoteDataSource.createExercise(
                exerciseDetails = exerciseDetails,
                idempotencyKey = mutation.idempotencyKey
            )
        } else {
            remoteDataSource.updateExercise(
                exerciseDetails = exerciseDetails,
                expectedVersion = mutation.expectedVersion,
                idempotencyKey = mutation.idempotencyKey
            )
        }

        return when (result) {
            is AppResult.Success -> {
                val updatedDto = result.value
                val updatedDetails = updatedDto.toExerciseDetails(existingImageId = existingImageId)
                exerciseDao.insertExerciseDetails(updatedDetails.toDbExerciseDetails())
                mutationDao.deleteMutation(mutation.id)
                Timber.d(
                    "Successfully synced mutation %s for exercise %s",
                    mutation.id,
                    mutation.exerciseId
                )
                AppResult.Success(Unit)
            }

            is AppResult.Error -> {
                val failure = result.error
                if (failure is ExerciseFailure.Conflict) {
                    val conflictDto = failure.conflictDto
                    val conflictServerJson = conflictDto?.let {
                        json.encodeToString(it.currentServerExercise)
                    }
                    mutationDao.updateMutation(
                        mutation.copy(
                            state = DbExerciseMutation.STATE_CONFLICT,
                            conflictServerJson = conflictServerJson
                        )
                    )
                    exerciseDao.updateSyncState(mutation.exerciseId, DbExercise.SYNC_STATE_CONFLICT)
                    Timber.w(
                        "Version conflict during mutation sync for exercise %s",
                        mutation.exerciseId
                    )
                } else {
                    val nextState = if (failure.isRetryable()) {
                        DbExerciseMutation.STATE_PENDING
                    } else {
                        DbExerciseMutation.STATE_ERROR
                    }
                    mutationDao.updateMutation(mutation.copy(state = nextState))
                    exerciseDao.updateSyncState(mutation.exerciseId, DbExercise.SYNC_STATE_ERROR)
                }
                AppResult.Error(failure)
            }
        }
    }
}
