package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.ExerciseCategoryLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.featureflags.DefaultFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultExerciseCategoryRepositoryTest {
    private val localDataSource = mockk<ExerciseCategoryLocalDataSource>(relaxed = true)
    private val remoteDataSource = mockk<ExerciseRemoteDataSource>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createRepository(onlineSyncEnabled: Boolean = true) =
        DefaultExerciseCategoryRepository(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            ioDispatcher = testDispatcher,
            featureFlagProvider = DefaultFeatureFlagProvider(
                overrides = mapOf(FeatureFlag.ONLINE_SYNC to onlineSyncEnabled)
            )
        )

    @Test
    fun `getAll syncs remote categories when online sync is enabled`() = runTest {
        val repository = createRepository(onlineSyncEnabled = true)
        val dbCategories = listOf(
            DbExerciseCategory("Chest"),
            DbExerciseCategory("Back")
        )
        coEvery { localDataSource.getAllCategories() } returns flowOf(dbCategories)
        coEvery { remoteDataSource.getCategories() } returns AppResult.Success(
            listOf(ExerciseCategory("Chest"), ExerciseCategory("Legs"))
        )

        val categories = repository.getAll().first()

        assertEquals(listOf(ExerciseCategory("Chest"), ExerciseCategory("Back")), categories)
        coVerify { localDataSource.insertCategories(any()) }
    }

    @Test
    fun `getAll skips remote sync when online sync is disabled`() = runTest {
        val repository = createRepository(onlineSyncEnabled = false)
        val dbCategories = listOf(
            DbExerciseCategory("Chest"),
            DbExerciseCategory("Back")
        )
        coEvery { localDataSource.getAllCategories() } returns flowOf(dbCategories)

        val categories = repository.getAll().first()

        assertEquals(listOf(ExerciseCategory("Chest"), ExerciseCategory("Back")), categories)
        coVerify(exactly = 0) { remoteDataSource.getCategories() }
        coVerify(exactly = 0) { localDataSource.insertCategories(any()) }
    }
}
