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

    @Volatile
    private var lastRemoteSyncTimestamp: Long = 0L

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

    override fun enqueueRemoteChangeSync(force: Boolean) {
        coroutineScope.launch {
            if (!authSessionProvider.isLoggedIn()) {
                Timber.d("Remote change sync worker not queued: User not logged in")
                return@launch
            }
            val autoSyncEnabled = syncPreferencesStorage.autoSyncEnabled.first()
            if (!autoSyncEnabled) {
                Timber.d("Remote change sync worker not queued: Auto sync disabled in DataStore")
                return@launch
            }

            val currentTime = System.currentTimeMillis()
            if (!force && (currentTime - lastRemoteSyncTimestamp < FIFTEEN_MINUTES_MS)) {
                Timber.d("Remote change sync worker throttled (last fetch was < 15 mins ago)")
                return@launch
            }

            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = OneTimeWorkRequestBuilder<ExerciseRemoteChangesSyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    REMOTE_CHANGES_WORKER_NAME,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
                lastRemoteSyncTimestamp = currentTime
                Timber.d("Enqueued ExerciseRemoteChangesSyncWorker with CONNECTED constraint")
            } catch (e: Exception) {
                Timber.w(e, "Failed to enqueue ExerciseRemoteChangesSyncWorker")
            }
        }
    }

    override fun cancelMutationSync() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(REMOTE_CHANGES_WORKER_NAME)
            Timber.d("Cancelled exercise sync workers")
        } catch (e: Exception) {
            Timber.w(e, "Failed to cancel exercise sync workers")
        }
    }

    companion object {
        const val WORKER_NAME = "ExerciseMutationWorker"
        const val REMOTE_CHANGES_WORKER_NAME = "ExerciseRemoteChangesSyncWorker"
        const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
    }
}
