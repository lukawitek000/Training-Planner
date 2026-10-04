package com.lukasz.witkowski.training.planner.exercise.delete

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteExerciseDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val vm = mockk<DeleteExerciseViewModel>(relaxed = true)

    @Test
    fun loading_is_displayed_when_exercise_is_not_yet_loaded() {
        every { vm.state } returns MutableStateFlow(DeleteExerciseUiState.Loading)

        composeTestRule.setContent {
            DeleteExerciseScreen(
                viewModel = vm,
                onDelete = {},
                onCancel = {}
            )
        }

        composeTestRule
            .onNodeWithTag("OverlayContent")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun dialog_is_displayed_when_exercise_is_loaded() {
        val exerciseName = "PushUp"
        every { vm.state } returns MutableStateFlow(DeleteExerciseUiState.LoadedExercise(exerciseName))

        composeTestRule.setContent {
            DeleteExerciseScreen(
                viewModel = vm,
                onDelete = {},
                onCancel = {}
            )
        }

        composeTestRule
            .onNodeWithTag("DeleteExerciseDialog")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("DeleteExerciseTitle")
            .assertIsDisplayed()
            .assertTextContains(exerciseName, substring = true)


        composeTestRule
            .onNodeWithTag("Loading")
            .assertIsNotDisplayed()
    }

    @Test
    fun on_delete_triggered_when_exercise_is_deleted() {
        val exerciseName = "PushUp"
        every { vm.state } returns MutableStateFlow(DeleteExerciseUiState.LoadedExercise(exerciseName))
        every { vm.deletionEvent } returns flow { emit(DeletionEvent.Success) }

        var isDeleteClicked = false
        var isCancelClicked = false
        composeTestRule.setContent {
            DeleteExerciseScreen(
                viewModel = vm,
                onDelete = { isDeleteClicked = true },
                onCancel = { isCancelClicked = true }
            )
        }

        composeTestRule
            .onNodeWithTag("confirmButton")
            .performClick()

        verify(exactly = 1) {
            vm.deleteExercise()
        }

        assertTrue(isDeleteClicked)
        assertFalse(isCancelClicked)
    }

    @Test
    fun on_cancel_triggered_when_deletion_is_cancelled() {
        val exerciseName = "PushUp"
        every { vm.state } returns MutableStateFlow(DeleteExerciseUiState.LoadedExercise(exerciseName))
        var isDeleteClicked = false
        var isCancelClicked = false
        composeTestRule.setContent {
            DeleteExerciseScreen(
                viewModel = vm,
                onDelete = { isDeleteClicked = true },
                onCancel = { isCancelClicked = true }
            )
        }

        composeTestRule
            .onNodeWithTag("dismissButton")
            .performClick()

        assertTrue(isCancelClicked)
        assertFalse(isDeleteClicked)
    }
}