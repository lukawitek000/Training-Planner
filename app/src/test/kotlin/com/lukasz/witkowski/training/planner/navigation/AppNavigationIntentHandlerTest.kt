package com.lukasz.witkowski.training.planner.navigation

import android.content.Intent
import androidx.navigation3.runtime.NavBackStack
import com.lukasz.witkowski.training.planner.notification.AppSessionNotificationContentIntentProvider
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow

class AppNavigationIntentHandlerTest {
    private val trainingSessionService: TrainingSessionService = mockk()
    private val handler = AppNavigationIntentHandler(trainingSessionService)

    @Test
    fun `returns false and leaves backStack untouched when extra is absent`() {
        val intent: Intent = mockk(relaxed = true)
        every {
            intent.getBooleanExtra(
                AppSessionNotificationContentIntentProvider.EXTRA_OPEN_ACTIVE_SESSION,
                false
            )
        } returns false
        val backStack = NavBackStack<TrainingPlannerNavKey>(TrainingPlansList)
        val navigator = TrainingPlannerNavigator(backStack)

        val handled = handler.handle(intent, navigator)

        assertFalse(handled)
        assertEquals(listOf<TrainingPlannerNavKey>(TrainingPlansList), backStack.toList())
        verify(exactly = 0) { intent.removeExtra(any()) }
    }

    @Test
    fun `consumes extra and navigates to active session when session is active`() {
        val planId = TrainingPlanId.create()
        val plan: TrainingPlan = mockk { every { id } returns planId }
        val activeState: TrainingSessionState.ExerciseState = mockk()
        every { trainingSessionService.trainingSessionState } returns MutableStateFlow(activeState)
        every { trainingSessionService.trainingPlan } returns plan

        val intent: Intent = mockk(relaxed = true)
        every {
            intent.getBooleanExtra(
                AppSessionNotificationContentIntentProvider.EXTRA_OPEN_ACTIVE_SESSION,
                false
            )
        } returns true

        val backStack = NavBackStack<TrainingPlannerNavKey>(TrainingPlansList)
        val navigator = TrainingPlannerNavigator(backStack)

        val handled = handler.handle(intent, navigator)

        assertTrue(handled)
        assertEquals(listOf(TrainingPlansList, TrainingSession(planId)), backStack.toList())
        verify(exactly = 1) {
            intent.removeExtra(AppSessionNotificationContentIntentProvider.EXTRA_OPEN_ACTIVE_SESSION)
        }
    }

    @Test
    fun `consumes extra and returns false when session is idle`() {
        every { trainingSessionService.trainingSessionState } returns
            MutableStateFlow(TrainingSessionState.IdleState)
        every { trainingSessionService.trainingPlan } returns null

        val intent: Intent = mockk(relaxed = true)
        every {
            intent.getBooleanExtra(
                AppSessionNotificationContentIntentProvider.EXTRA_OPEN_ACTIVE_SESSION,
                false
            )
        } returns true

        val backStack = NavBackStack<TrainingPlannerNavKey>(TrainingPlansList)
        val navigator = TrainingPlannerNavigator(backStack)

        val handled = handler.handle(intent, navigator)

        assertFalse(handled)
        assertEquals(listOf<TrainingPlannerNavKey>(TrainingPlansList), backStack.toList())
        verify(exactly = 1) {
            intent.removeExtra(AppSessionNotificationContentIntentProvider.EXTRA_OPEN_ACTIVE_SESSION)
        }
    }
}
