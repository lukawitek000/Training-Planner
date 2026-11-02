package com.lukasz.witkowski.training.planner.training.presentation.delete

import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.training.TestData
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class DeleteTrainingPlanViewModelTest {
    private val service: TrainingPlanService = mockk()
    private val id = TrainingPlanId.create()
    private lateinit var vm: DeleteTrainingPlanViewModel

    private fun givenVm() {
        vm = DeleteTrainingPlanViewModel(
            trainingPlanService = service,
            trainingPlanId = id
        )
    }

    @Test
    fun `when plan does not exist emit error state`() = runTest {
        every { service.getTrainingPlanById(any()) } returns flowOf(null)
        givenVm()

        vm.state.test {
            assertEquals(DeleteTrainingPlanUiState.Loading, awaitItem())
            assertEquals(DeleteTrainingPlanUiState.Error("Training plan not found"), awaitItem())
        }
    }

    @Test
    fun `when plan is loaded emit success state`() = runTest {
        val plan = TestData.FULL_BODY_TRAINING_PLAN
        every { service.getTrainingPlanById(any()) } returns flowOf(plan)
        givenVm()

        vm.state.test {
            assertEquals(DeleteTrainingPlanUiState.Loading, awaitItem())
            assertEquals(DeleteTrainingPlanUiState.LoadedTrainingPlan(plan.title), awaitItem())
        }
    }

    @Test
    fun `when deletion succeeds emit success event`() = runTest {
        every { service.getTrainingPlanById(any()) } returns
            flowOf(TestData.FULL_BODY_TRAINING_PLAN)
        coEvery { service.deleteTrainingPlan(any()) } returns Result.success(Unit)
        givenVm()

        vm.deletionEvent.test {
            vm.deleteTrainingPlan()
            assertEquals(DeleteTrainingPlanEvent.Success, awaitItem())
        }
    }

    @Test
    fun `when deletion fails emit failure event`() = runTest {
        every { service.getTrainingPlanById(any()) } returns
            flowOf(TestData.FULL_BODY_TRAINING_PLAN)
        coEvery { service.deleteTrainingPlan(any()) } returns
            Result.failure(IllegalStateException("Failed deletion"))
        givenVm()

        vm.deletionEvent.test {
            vm.deleteTrainingPlan()
            assertEquals(DeleteTrainingPlanEvent.Failure, awaitItem())
        }
    }
}
