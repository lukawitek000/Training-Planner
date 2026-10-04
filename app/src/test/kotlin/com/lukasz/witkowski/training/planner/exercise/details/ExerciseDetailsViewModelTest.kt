package com.lukasz.witkowski.training.planner.exercise.details

import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ExerciseDetailsViewModelTest {
    private val service: ExerciseService = mockk()
    private val id = ExerciseId.create()
    private lateinit var vm: ExerciseDetailsViewModel

    private fun givenVm() {
        vm = ExerciseDetailsViewModel(
            service = service,
            exerciseId = id
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
        givenVm()
        vm.state.test {
            assertEquals(ExerciseDetailsState.Loading, awaitItem())
            assertEquals(
                expected = ExerciseDetailsState.Success(TestData.PRESENTATION_PUSH_UPS_DETAILS),
                actual = awaitItem()
            )
        }
    }
}
