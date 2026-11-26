package com.lukasz.witkowski.training.planner.navigation

import androidx.navigation3.runtime.NavBackStack
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainingPlannerNavigatorTest {
    @Test
    fun `openActiveTrainingSession pushes session when not in backStack`() {
        val planId = TrainingPlanId.create()
        val backStack = NavBackStack<TrainingPlannerNavKey>(TrainingPlansList)
        val navigator = TrainingPlannerNavigator(backStack)

        navigator.openActiveTrainingSession(planId)

        assertEquals(
            listOf(TrainingPlansList, TrainingSession(planId)),
            backStack.toList()
        )
    }

    @Test
    fun `openActiveTrainingSession is no-op when already on active session`() {
        val planId = TrainingPlanId.create()
        val backStack = NavBackStack<TrainingPlannerNavKey>(TrainingPlansList, TrainingSession(planId))
        val navigator = TrainingPlannerNavigator(backStack)

        navigator.openActiveTrainingSession(planId)

        assertEquals(
            listOf(TrainingPlansList, TrainingSession(planId)),
            backStack.toList()
        )
    }

    @Test
    fun `openActiveTrainingSession pops back to existing session deeper in backStack`() {
        val planId = TrainingPlanId.create()
        val backStack =
            NavBackStack<TrainingPlannerNavKey>(
                TrainingPlansList,
                TrainingSession(planId),
                UserProfile
            )
        val navigator = TrainingPlannerNavigator(backStack)

        navigator.openActiveTrainingSession(planId)

        assertEquals(
            listOf(TrainingPlansList, TrainingSession(planId)),
            backStack.toList()
        )
    }
}
