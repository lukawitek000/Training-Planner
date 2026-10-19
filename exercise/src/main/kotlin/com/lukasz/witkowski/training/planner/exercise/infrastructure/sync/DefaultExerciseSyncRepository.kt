package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExercise
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseMutation
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseWithCategories
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseMutationDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.toExercise2
import com.lukasz.witkowski.training.planner.exercise.infrastructure.toDbExercise
import com.lukasz.witkowski.training.planner.exercise.infrastructure.toDbExerciseCategory
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
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
                        val domainExercise = dto.toExercise2()
                        DbExerciseWithCategories(
                            exercise = domainExercise.toDbExercise(),
                            categories = domainExercise.categories.map { it.toDbExerciseCategory() }
                        )
                    }
                    exerciseDao.applySyncChanges(
                        upserts = upsertDbItems,
                        deletedIds = syncResponse.deletedIds,
                        newVersion = syncResponse.currentVersion
                    )
                    Timber.d("Exercise sync succeeded. Local sync version updated to %d", syncResponse.currentVersion)
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
                    anyFailed = true
                    lastError = result.error
                }
            }

            if (anyFailed) AppResult.Error(lastError) else AppResult.Success(Unit)
        }

    private suspend fun syncMutation(
        mutation: DbExerciseMutation
    ): AppResult<Unit, ExerciseFailure> {
        val domainExercise = Exercise2(
            id = ExerciseId(mutation.exerciseId),
            name = mutation.name,
            description = mutation.description,
            categories = mutation.categoryNames.split(",").filter { it.isNotBlank() }.map { ExerciseCategory(it) }.toSet(),
            metadata = ExerciseMetadata(
                revision = mutation.expectedVersion,
                syncState = ExerciseSyncState.PENDING_UPLOAD
            )
        )

        val beginnerRec = ExerciseRecommendation(
            sets = mutation.beginnerSets ?: 0,
            reps = mutation.beginnerReps ?: 0,
            restTime = (mutation.beginnerRestTime ?: 0L).milliseconds,
            weightInKg = mutation.beginnerWeight
        )
        val intermediateRec = ExerciseRecommendation(
            sets = mutation.intermediateSets ?: 0,
            reps = mutation.intermediateReps ?: 0,
            restTime = (mutation.intermediateRestTime ?: 0L).milliseconds,
            weightInKg = mutation.intermediateWeight
        )
        val advancedRec = ExerciseRecommendation(
            sets = mutation.advancedSets ?: 0,
            reps = mutation.advancedReps ?: 0,
            restTime = (mutation.advancedRestTime ?: 0L).milliseconds,
            weightInKg = mutation.advancedWeight
        )

        val exerciseDetails = ExerciseDetails(
            exercise = domainExercise,
            beginnerRecommendation = beginnerRec,
            intermediateRecommendation = intermediateRec,
            advancedRecommendation = advancedRec
        )

        mutationDao.updateMutation(mutation.copy(state = "IN_PROGRESS"))

        val result = remoteDataSource.updateExercise(
            exerciseDetails = exerciseDetails,
            expectedVersion = mutation.expectedVersion,
            idempotencyKey = mutation.idempotencyKey
        )

        return when (result) {
            is AppResult.Success -> {
                val updatedDto = result.value
                val domainEx = updatedDto.toExercise2()
                exerciseDao.insert(domainEx.toDbExercise())
                mutationDao.deleteMutation(mutation.id)
                Timber.d("Successfully synced mutation %s for exercise %s", mutation.id, mutation.exerciseId)
                AppResult.Success(Unit)
            }

            is AppResult.Error -> {
                val failure = result.error
                if (failure is ExerciseFailure.Conflict) {
                    val conflictDto = failure.conflictDto
                    val conflictServerJson = conflictDto?.let { json.encodeToString(it.currentServerExercise) }
                    mutationDao.updateMutation(
                        mutation.copy(
                            state = "CONFLICT",
                            conflictServerJson = conflictServerJson
                        )
                    )
                    exerciseDao.updateSyncState(mutation.exerciseId, DbExercise.SYNC_STATE_ERROR)
                    Timber.w("Version conflict during mutation sync for exercise %s", mutation.exerciseId)
                } else {
                    mutationDao.updateMutation(mutation.copy(state = "PENDING"))
                    exerciseDao.updateSyncState(mutation.exerciseId, DbExercise.SYNC_STATE_ERROR)
                }
                AppResult.Error(failure)
            }
        }
    }
}
