package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ExerciseRemoteChangesSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams),
    KoinComponent {

    private val syncRepository: ExerciseSyncRepository by inject()
    private val featureFlagProvider: FeatureFlagProvider by inject()

    override suspend fun doWork(): Result {
        if (!featureFlagProvider.isEnabled(FeatureFlag.ONLINE_SYNC)) {
            Timber.d("Skipping ExerciseRemoteChangesSyncWorker: ONLINE_SYNC is disabled")
            return Result.success()
        }
        Timber.d("ExerciseRemoteChangesSyncWorker executing synchronizeExercises()")
        return when (syncRepository.synchronizeExercises()) {
            is AppResult.Success -> Result.success()
            is AppResult.Error -> Result.retry()
        }
    }
}
