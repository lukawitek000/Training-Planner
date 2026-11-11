package com.lukasz.witkowski.training.planner.navigation

import android.content.Context
import androidx.navigation3.runtime.NavBackStack
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavItemTest {
    private val context: Context = mockk(relaxed = true)

    @BeforeTest
    fun setUp() {
        every { context.getString(any()) } returns "test_string"
    }

    @Test
    fun `toUiConfig when current key is ExerciseDetails with isEditable false hides edit and delete menu items`() {
        val exerciseId = ExerciseId.create()
        val backStack = NavBackStack<TrainingPlannerNavKey>(ExerciseDetails(exerciseId, isEditable = false))
        val navigator = TrainingPlannerNavigator(backStack)

        val uiConfig = navigator.toUiConfig(context)

        assertTrue(uiConfig.topBarConfig.menuItems.isEmpty())
        assertTrue(uiConfig.topBarConfig.hasBackArrow)
    }

    @Test
    fun `toUiConfig when current key is ExerciseDetails with isEditable true shows edit and delete menu items`() {
        val exerciseId = ExerciseId.create()
        val backStack = NavBackStack<TrainingPlannerNavKey>(ExerciseDetails(exerciseId, isEditable = true))
        val navigator = TrainingPlannerNavigator(backStack)

        val uiConfig = navigator.toUiConfig(context)

        assertEquals(2, uiConfig.topBarConfig.menuItems.size)
        assertTrue(uiConfig.topBarConfig.menuItems.any { it.action is TopBarAction.EditExercise })
        assertTrue(uiConfig.topBarConfig.menuItems.any { it.action is TopBarAction.DeleteExercise })
    }

    @Test
    fun `toUiConfig when current key is DeleteExercise shows edit and delete menu items`() {
        val exerciseId = ExerciseId.create()
        val backStack = NavBackStack<TrainingPlannerNavKey>(DeleteExercise(exerciseId))
        val navigator = TrainingPlannerNavigator(backStack)

        val uiConfig = navigator.toUiConfig(context)

        assertEquals(2, uiConfig.topBarConfig.menuItems.size)
        assertTrue(uiConfig.topBarConfig.menuItems.any { it.action is TopBarAction.EditExercise })
        assertTrue(uiConfig.topBarConfig.menuItems.any { it.action is TopBarAction.DeleteExercise })
    }
}
