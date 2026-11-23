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
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.network.AuthSessionProvider
import com.lukasz.witkowski.training.planner.network.SyncPreferencesStorage
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.time.SystemTimeProvider
import com.lukasz.witkowski.training.planner.shared.time.TimeProvider
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class WorkManagerExerciseMutationScheduler(
    private val context: Context,
    private val authSessionProvider: AuthSessionProvider,
    private val syncPreferencesStorage: SyncPreferencesStorage,
    private val featureFlagProvider: FeatureFlagProvider,
    private val coroutineScope: CoroutineScope,
    private val exerciseSyncRepositoryProvider: (() -> ExerciseSyncRepository)? = null,
    private val timeProvider: TimeProvider = SystemTimeProvider()
) : ExerciseMutationScheduler,
    SyncScheduler {

    private val syncMutex = Mutex()

    @Volatile
    private var lastRemoteSyncElapsed: Duration? = null

    override fun enqueueMutationSync() {
        coroutineScope.launch {
            syncMutex.withLock {
                if (!featureFlagProvider.isEnabled(FeatureFlag.ONLINE_SYNC)) {
                    Timber.d("Worker not queued: Online sync feature flag disabled")
                    return@launch
                }
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
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        workRequest
                    )
                    Timber.d(
                        "Enqueued ExerciseMutationWorker with CONNECTED constraint and backoff"
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Failed to enqueue ExerciseMutationWorker")
                }
            }
        }
    }

    override fun enqueueRemoteChangeSync(force: Boolean) {
        coroutineScope.launch {
            syncMutex.withLock {
                if (!featureFlagProvider.isEnabled(FeatureFlag.ONLINE_SYNC)) {
                    Timber.d(
                        "Remote change sync worker not queued: Online sync feature flag disabled"
                    )
                    return@launch
                }
                if (!authSessionProvider.isLoggedIn()) {
                    Timber.d("Remote change sync worker not queued: User is not logged in")
                    return@launch
                }
                val autoSyncEnabled = syncPreferencesStorage.autoSyncEnabled.first()
                if (!autoSyncEnabled) {
                    Timber.d(
                        "Remote change sync worker not queued: Auto sync disabled in DataStore"
                    )
                    return@launch
                }

                val currentElapsed = timeProvider.elapsedTime()
                val lastSync = lastRemoteSyncElapsed
                if (!force &&
                    lastSync != null &&
                    (currentElapsed - lastSync) < REMOTE_SYNC_THROTTLE
                ) {
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
                    lastRemoteSyncElapsed = currentElapsed
                    Timber.d("Enqueued ExerciseRemoteChangesSyncWorker with CONNECTED constraint")
                } catch (e: Exception) {
                    Timber.w(e, "Failed to enqueue ExerciseRemoteChangesSyncWorker")
                }
            }
        }
    }

    override fun cancelMutationSync() {
        lastRemoteSyncElapsed = null
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(REMOTE_CHANGES_WORKER_NAME)
            Timber.d("Cancelled exercise sync workers")
        } catch (e: Exception) {
            Timber.w(e, "Failed to cancel exercise sync workers")
        }
    }

    override suspend fun clearLocalSyncData(keepLocalData: Boolean) {
        syncMutex.withLock {
            cancelMutationSync()
        }
        exerciseSyncRepositoryProvider?.invoke()?.onUserLogout(keepLocalData)
    }

    companion object {
        const val WORKER_NAME = "ExerciseMutationWorker"
        const val REMOTE_CHANGES_WORKER_NAME = "ExerciseRemoteChangesSyncWorker"
        val REMOTE_SYNC_THROTTLE = 15.minutes
    }
}
