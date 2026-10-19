package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.domain.isRetryable
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ExerciseMutationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val syncRepository: ExerciseSyncRepository by inject()

    override suspend fun doWork(): Result {
        Timber.d("ExerciseMutationWorker executing pending mutations")
        return when (val result = syncRepository.processPendingMutations()) {
            is AppResult.Success -> Result.success()
            is AppResult.Error -> if (result.error.isRetryable()) Result.retry() else Result.success()
        }
    }
}
