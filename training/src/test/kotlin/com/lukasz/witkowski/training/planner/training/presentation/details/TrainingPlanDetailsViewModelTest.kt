package com.lukasz.witkowski.training.planner.training.presentation.details

import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.TestData
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainingPlanDetailsViewModelTest {
    private val service: TrainingPlanService = mockk()
    private val planId = TrainingPlanId.create()

    @Test
    fun `when plan is found emit success state`() = runTest {
        val plan = TestData.FULL_BODY_TRAINING_PLAN
        every { service.getTrainingPlanById(planId) } returns flowOf(plan)

        val vm = TrainingPlanDetailsViewModel(
            trainingPlanService = service,
            trainingPlanId = planId
        )

        vm.trainingPlan.test {
            assertEquals(ResultHandler.Loading, awaitItem())
            assertEquals(ResultHandler.Success(plan), awaitItem())
        }
    }

    @Test
    fun `when plan is not found emit error state`() = runTest {
        every { service.getTrainingPlanById(planId) } returns flowOf(null)

        val vm = TrainingPlanDetailsViewModel(
            trainingPlanService = service,
            trainingPlanId = planId
        )

        vm.trainingPlan.test {
            assertEquals(ResultHandler.Loading, awaitItem())
            assertEquals(ResultHandler.Error("Plan not found"), awaitItem())
        }
    }
}
