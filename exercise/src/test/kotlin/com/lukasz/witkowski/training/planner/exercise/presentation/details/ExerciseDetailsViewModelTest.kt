package com.lukasz.witkowski.training.planner.exercise.presentation.details

import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import com.lukasz.witkowski.training.planner.shared.featureflags.DefaultFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseDetailsViewModelTest {
    private val service: ExerciseService = mockk()
    private val syncRepository: ExerciseSyncRepository = mockk(relaxed = true)
    private val syncScheduler: SyncScheduler = mockk(relaxed = true)
    private val id = ExerciseId.create()
    private lateinit var vm: ExerciseDetailsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun givenVm(
        featureFlagProvider: FeatureFlagProvider = DefaultFeatureFlagProvider(
            overrides = mapOf(FeatureFlag.ONLINE_SYNC to true)
        )
    ) {
        vm = ExerciseDetailsViewModel(
            service = service,
            exerciseId = id,
            syncRepository = syncRepository,
            syncScheduler = syncScheduler,
            featureFlagProvider = featureFlagProvider
        )
    }

    @Test
    fun `when service returns null the failure state is emitted`() = runTest {
        coEvery { service.getExerciseDetailsById(any()) } returns flowOf(null)
        givenVm()
        vm.state.test {
            assertEquals(ExerciseDetailsState.Loading, awaitItem())
            assertEquals(
                expected = ExerciseDetailsState.Failure("Exercise not found"),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `when service throws the failure state is emitted`() = runTest {
        val message = "Expected failure"
        coEvery { service.getExerciseDetailsById(any()) } returns flow {
            throw IllegalStateException(message)
        }
        givenVm()
        vm.state.test {
            assertEquals(ExerciseDetailsState.Loading, awaitItem())
            assertEquals(
                expected = ExerciseDetailsState.Failure(message),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `when service returns exercise details the success state is emitted`() = runTest {
        coEvery { service.getExerciseDetailsById(any()) } returns flow {
            emit(TestData.PUSH_UPS_DETAILS)
        }
        coEvery { service.readImageReferences(any()) } returns emptyList()
        givenVm()
        vm.state.test {
            assertEquals(ExerciseDetailsState.Loading, awaitItem())
            assertEquals(
                expected = ExerciseDetailsState.Success(TestData.PRESENTATION_PUSH_UPS_DETAILS),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `when exercise has images they are loaded into presentation model`() = runTest {
        val imageReferences = listOf(
            TestData.SAMPLE_IMAGE_REFERENCE,
            TestData.SAMPLE_IMAGE_REFERENCE.copy(
                imageId = TestData.IMAGE_ID_2,
                path = "path/to/image2.jpg"
            )
        )
        coEvery { service.getExerciseDetailsById(id) } returns flowOf(TestData.PUSH_UPS_DETAILS)
        coEvery { service.readImageReferences(id) } returns imageReferences

        givenVm()

        vm.state.test {
            assertEquals(ExerciseDetailsState.Loading, awaitItem())
            val success = awaitItem() as ExerciseDetailsState.Success
            assertEquals(imageReferences, success.details.exercise.images)
        }
    }

    @Test
    fun `when resolveConflictKeepLocal is called, syncRepository and syncScheduler are invoked`() =
        runTest {
            coEvery { service.getExerciseDetailsById(any()) } returns
                flowOf(TestData.PUSH_UPS_DETAILS)
            coEvery { syncRepository.resolveConflictKeepLocal(id) } returns AppResult.Success(Unit)
            givenVm()

            vm.resolveConflictKeepLocal()
            advanceUntilIdle()

            coVerify(exactly = 1) { syncRepository.resolveConflictKeepLocal(id) }
            verify(exactly = 1) { syncScheduler.enqueueMutationSync() }
        }

    @Test
    fun `when resolveConflictAcceptServer is called, syncRepository is invoked`() = runTest {
        coEvery { service.getExerciseDetailsById(any()) } returns flowOf(TestData.PUSH_UPS_DETAILS)
        coEvery { syncRepository.resolveConflictAcceptServer(id) } returns AppResult.Success(Unit)
        givenVm()

        vm.resolveConflictAcceptServer()
        advanceUntilIdle()

        coVerify(exactly = 1) { syncRepository.resolveConflictAcceptServer(id) }
    }

    @Test
    fun `when ONLINE_SYNC is false, conflict exercise does not trigger server fetch or actions`() =
        runTest {
            val conflictDetails = TestData.PUSH_UPS_DETAILS.copy(
                exercise = TestData.PUSH_UPS_EXERCISE.copy(
                    metadata = TestData.PUSH_UPS_EXERCISE.metadata.copy(
                        syncState = ExerciseSyncState.CONFLICT
                    )
                )
            )
            coEvery { service.getExerciseDetailsById(any()) } returns flowOf(conflictDetails)
            coEvery { service.readImageReferences(any()) } returns emptyList()
            givenVm(
                featureFlagProvider = DefaultFeatureFlagProvider(
                    overrides = mapOf(FeatureFlag.ONLINE_SYNC to false)
                )
            )

            vm.state.test {
                assertEquals(ExerciseDetailsState.Loading, awaitItem())
                awaitItem()
            }
            vm.openConflictDialog()
            vm.resolveConflictKeepLocal()
            vm.resolveConflictAcceptServer()
            vm.retrySync()
            advanceUntilIdle()

            coVerify(exactly = 0) { syncRepository.getConflictServerExercise(any()) }
            coVerify(exactly = 0) { syncRepository.resolveConflictKeepLocal(any()) }
            coVerify(exactly = 0) { syncRepository.resolveConflictAcceptServer(any()) }
            verify(exactly = 0) { syncScheduler.enqueueMutationSync() }
        }
}
