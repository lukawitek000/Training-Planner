package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import android.content.Context
import com.lukasz.witkowski.training.planner.network.AuthSessionProvider
import com.lukasz.witkowski.training.planner.network.SyncPreferencesStorage
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class WorkManagerExerciseMutationSchedulerTest {
    private val context = mockk<Context>(relaxed = true)
    private val authSessionProvider = mockk<AuthSessionProvider>()
    private val syncPreferencesStorage = mockk<SyncPreferencesStorage>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val scheduler = WorkManagerExerciseMutationScheduler(
        context = context,
        authSessionProvider = authSessionProvider,
        syncPreferencesStorage = syncPreferencesStorage,
        exerciseSyncRepositoryProvider = { mockk(relaxed = true) },
        coroutineScope = testScope
    )

    @Test
    fun `enqueueMutationSync does not throw exception when user is unauthenticated`() = runTest {
        coEvery { authSessionProvider.isLoggedIn() } returns false
        coEvery { syncPreferencesStorage.autoSyncEnabled } returns flowOf(true)

        scheduler.enqueueMutationSync()
    }

    @Test
    fun `enqueueMutationSync does not throw exception when auto cloud sync is disabled`() =
        runTest {
            coEvery { authSessionProvider.isLoggedIn() } returns true
            coEvery { syncPreferencesStorage.autoSyncEnabled } returns flowOf(false)

            scheduler.enqueueMutationSync()
        }
}
