package com.lukasz.witkowski.training.planner.exercise.details

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.lukasz.witkowski.training.planner.exercise.TestData
import org.junit.Rule
import org.junit.Test

class ExerciseDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun success_state_displays_exercise_details() {
        val exercise = TestData.PRESENTATION_PUSH_UPS_DETAILS.exercise
        val state = ExerciseDetailsState.Success(
            details = TestData.PRESENTATION_PUSH_UPS_DETAILS
        )

        whenSetContent(state = state)

        composeTestRule
            .onNodeWithText(exercise.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(exercise.description)
            .assertIsDisplayed()
    }

    @Test
    fun loading_state_displays_loading_indicator() {
        val state = ExerciseDetailsState.Loading

        whenSetContent(state)

        composeTestRule
            .onNodeWithTag("loading")
            .assertIsDisplayed()
    }

    @Test
    fun failure_state_displays_error() {
        val failureMessage = "Failed"
        val state = ExerciseDetailsState.Failure(failureMessage)

        whenSetContent(state)

        composeTestRule
            .onNodeWithText("Failed to load exercise details")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(failureMessage)
            .assertIsDisplayed()
    }

    private fun whenSetContent(state: ExerciseDetailsState) {
        composeTestRule.setContent {
            ExerciseDetailsScreenContent(
                state = state,
            )
        }
    }
}