package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.lukasz.witkowski.training.planner.network.AuthSessionProvider
import com.lukasz.witkowski.training.planner.network.SyncPreferencesStorage
import com.lukasz.witkowski.training.planner.shared.featureflags.DefaultFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class WorkManagerExerciseMutationSchedulerTest {
    private val context = mockk<Context>(relaxed = true)
    private val authSessionProvider = mockk<AuthSessionProvider>()
    private val syncPreferencesStorage = mockk<SyncPreferencesStorage>()
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val timeProvider = TestTimeProvider()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
    }

    @After
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    private fun createScheduler(onlineSyncEnabled: Boolean = true) =
        WorkManagerExerciseMutationScheduler(
            context = context,
            authSessionProvider = authSessionProvider,
            syncPreferencesStorage = syncPreferencesStorage,
            featureFlagProvider = DefaultFeatureFlagProvider(
                overrides = mapOf(FeatureFlag.ONLINE_SYNC to onlineSyncEnabled)
            ),
            exerciseSyncRepositoryProvider = { mockk(relaxed = true) },
            coroutineScope = testScope,
            timeProvider = timeProvider
        )

    @Test
    fun `enqueueMutationSync does not throw exception when user is unauthenticated`() = runTest {
        val scheduler = createScheduler(onlineSyncEnabled = true)
        coEvery { authSessionProvider.isLoggedIn() } returns false
        coEvery { syncPreferencesStorage.autoSyncEnabled } returns flowOf(true)

        scheduler.enqueueMutationSync()
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `enqueueMutationSync does not throw exception when auto cloud sync is disabled`() =
        runTest {
            val scheduler = createScheduler(onlineSyncEnabled = true)
            coEvery { authSessionProvider.isLoggedIn() } returns true
            coEvery { syncPreferencesStorage.autoSyncEnabled } returns flowOf(false)

            scheduler.enqueueMutationSync()
            verify(exactly = 0) {
                workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
            }
        }

    @Test
    fun `enqueueMutationSync short-circuits safely when online sync flag is disabled`() = runTest {
        val scheduler = createScheduler(onlineSyncEnabled = false)
        scheduler.enqueueMutationSync()
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `enqueueRemoteChangeSync short-circuits safely when online sync flag is disabled`() =
        runTest {
            val scheduler = createScheduler(onlineSyncEnabled = false)
            scheduler.enqueueRemoteChangeSync(force = true)
            verify(exactly = 0) {
                workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
            }
        }

    @Test
    fun `enqueueRemoteChangeSync throttles using monotonic elapsedTime and resets on cancel`() =
        runTest {
            val scheduler = createScheduler(onlineSyncEnabled = true)
            coEvery { authSessionProvider.isLoggedIn() } returns true
            every { syncPreferencesStorage.autoSyncEnabled } returns flowOf(true)

            // 1. First sync right after boot (elapsedMillis = 0L) is NOT throttled
            scheduler.enqueueRemoteChangeSync(force = false)
            verify(exactly = 1) {
                workManager.enqueueUniqueWork(
                    WorkManagerExerciseMutationScheduler.REMOTE_CHANGES_WORKER_NAME,
                    ExistingWorkPolicy.KEEP,
                    any<OneTimeWorkRequest>()
                )
            }

            // 2. Call 10 minutes later is throttled
            timeProvider.advanceBy(10.minutes)
            scheduler.enqueueRemoteChangeSync(force = false)
            verify(exactly = 1) {
                workManager.enqueueUniqueWork(
                    WorkManagerExerciseMutationScheduler.REMOTE_CHANGES_WORKER_NAME,
                    ExistingWorkPolicy.KEEP,
                    any<OneTimeWorkRequest>()
                )
            }

            // 3. Call after 15 minutes total elapse is allowed
            timeProvider.advanceBy(5.minutes)
            scheduler.enqueueRemoteChangeSync(force = false)
            verify(exactly = 2) {
                workManager.enqueueUniqueWork(
                    WorkManagerExerciseMutationScheduler.REMOTE_CHANGES_WORKER_NAME,
                    ExistingWorkPolicy.KEEP,
                    any<OneTimeWorkRequest>()
                )
            }

            // 4. Forced sync bypasses throttle within window
            timeProvider.advanceBy(1.minutes)
            scheduler.enqueueRemoteChangeSync(force = true)
            verify(exactly = 3) {
                workManager.enqueueUniqueWork(
                    WorkManagerExerciseMutationScheduler.REMOTE_CHANGES_WORKER_NAME,
                    ExistingWorkPolicy.KEEP,
                    any<OneTimeWorkRequest>()
                )
            }

            // 5. cancelMutationSync resets throttle state
            scheduler.cancelMutationSync()
            scheduler.enqueueRemoteChangeSync(force = false)
            verify(exactly = 4) {
                workManager.enqueueUniqueWork(
                    WorkManagerExerciseMutationScheduler.REMOTE_CHANGES_WORKER_NAME,
                    ExistingWorkPolicy.KEEP,
                    any<OneTimeWorkRequest>()
                )
            }
        }
}
