package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseWithCategories
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.toExercise2
import com.lukasz.witkowski.training.planner.exercise.infrastructure.toDbExercise
import com.lukasz.witkowski.training.planner.exercise.infrastructure.toDbExerciseCategory
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class DefaultExerciseSyncRepository(
    private val exerciseDao: ExerciseDao,
    private val remoteDataSource: ExerciseRemoteDataSource,
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
}
