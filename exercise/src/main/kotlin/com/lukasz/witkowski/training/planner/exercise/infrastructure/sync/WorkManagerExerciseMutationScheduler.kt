package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMutationScheduler
import com.lukasz.witkowski.training.planner.network.AuthSessionProvider
import com.lukasz.witkowski.training.planner.network.SyncPreferencesStorage
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class WorkManagerExerciseMutationScheduler(
    private val context: Context,
    private val authSessionProvider: AuthSessionProvider,
    private val syncPreferencesStorage: SyncPreferencesStorage,
    private val coroutineScope: CoroutineScope
) : ExerciseMutationScheduler, SyncScheduler {

    override fun enqueueMutationSync() {
        coroutineScope.launch {
            if (!authSessionProvider.isLoggedIn()) {
                Timber.d("Worker not queued: User is not logged in")
                return@launch
            }
            val autoSyncEnabled = syncPreferencesStorage.autoSyncEnabled.first()
            if (!autoSyncEnabled) {
                Timber.d("Worker not queued: Auto cloud sync disabled in DataStore")
                return@launch
            }

            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = OneTimeWorkRequestBuilder<ExerciseMutationWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    WORKER_NAME,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
                Timber.d("Enqueued ExerciseMutationWorker with CONNECTED constraint and backoff policy")
            } catch (e: Exception) {
                Timber.w(e, "Failed to enqueue ExerciseMutationWorker")
            }
        }
    }

    override fun cancelMutationSync() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
            Timber.d("Cancelled ExerciseMutationWorker")
        } catch (e: Exception) {
            Timber.w(e, "Failed to cancel ExerciseMutationWorker")
        }
    }

    companion object {
        const val WORKER_NAME = "ExerciseMutationWorker"
    }
}
