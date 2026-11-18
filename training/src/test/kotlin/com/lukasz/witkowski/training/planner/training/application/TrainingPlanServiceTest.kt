package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.training.TestData
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class TrainingPlanServiceTest {
    private val repository: TrainingPlanRepository = mockk()
    private val service = TrainingPlanService(repository)

    @Test
    fun `useTrainingPlan delegates to repository`() = runTest {
        val plan = TestData.FULL_BODY_TRAINING_PLAN
        coEvery { repository.useTrainingPlan(plan.id) } returns Result.success(plan)

        val result = service.useTrainingPlan(plan.id)

        assertTrue(result.isSuccess)
        assertEquals(plan, result.getOrThrow())
        coVerify(exactly = 1) { repository.useTrainingPlan(plan.id) }
    }
}
