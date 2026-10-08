package com.lukasz.witkowski.training.planner.exercise.delete

import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteExerciseViewModelTest {
    private val service: ExerciseService = mockk()
    private val id = ExerciseId.create()
    private lateinit var vm: DeleteExerciseViewModel

    private fun givenVm() {
        vm = DeleteExerciseViewModel(
            service = service,
            exerciseId = id
        )
    }

    @Test
    fun `when exercise does not exist emit error state`() = runTest {
        every { service.getExerciseDetailsById(any()) } returns flowOf(null)
        givenVm()

        vm.state.test {
            assertEquals(DeleteExerciseUiState.Loading, awaitItem())
            assertEquals(DeleteExerciseUiState.Error("Exercise not found"), awaitItem())
        }
    }

    @Test
    fun `when exercise is loaded emit success state`() = runTest {
        val details = TestData.PUSH_UPS_DETAILS
        every { service.getExerciseDetailsById(any()) } returns flowOf(details)
        givenVm()
        vm.state.test {
            assertEquals(DeleteExerciseUiState.Loading, awaitItem())
            val expected = DeleteExerciseUiState.LoadedExercise(details.exercise.name)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `when exercise deletion fail emit failure event`() = runTest {
        every { service.getExerciseDetailsById(any()) } returns flowOf(TestData.PUSH_UPS_DETAILS)
        coEvery { service.deleteExercise(any()) } coAnswers {
            throw IllegalStateException("Failed saving")
        }
        givenVm()
        vm.deletionEvent.test {
            vm.deleteExercise()
            assertEquals(DeletionEvent.Failure, awaitItem())
        }
    }

    @Test
    fun `when exercise deletion success emit success event`() = runTest {
        every { service.getExerciseDetailsById(any()) } returns flowOf(TestData.PUSH_UPS_DETAILS)
        coEvery { service.deleteExercise(any()) } returns Unit
        givenVm()
        vm.deletionEvent.test {
            vm.deleteExercise()
            assertEquals(DeletionEvent.Success, awaitItem())
        }
    }
}