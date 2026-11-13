package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionUiState
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w400dp-h900dp")
class TrainingSessionSummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysWorkoutCompletionAndSummaryMetrics() {
        val plan = TRAINING_PLAN
        val exerciseId = plan.exercises.first().id
        val statistics =
            TrainingStatistics(
                trainingPlanId = plan.id,
                totalTime = 2700.seconds,
                date = Date(0L),
                exercisesStatistics =
                listOf(
                    ExerciseStatistics(
                        trainingExerciseId = exerciseId,
                        attemptsStatistics =
                        listOf(
                            ExerciseAttemptStatistics(
                                trainingExerciseId = exerciseId,
                                time = 30.seconds,
                                set = 1,
                                actualReps = 5,
                                actualWeightInKg = 140.0,
                                completed = true
                            )
                        )
                    )
                )
            )

        val domainState =
            TrainingSessionState.SummaryState(
                statistics = statistics,
                selectedOverallRpe = null,
                notes = ""
            )
        val uiState =
            TrainingSessionUiState.Summary(
                domainState = domainState,
                trainingPlan = ResultHandler.Success(plan)
            )

        composeTestRule.setContent {
            TrainingPlannerTheme {
                TrainingSessionSummaryScreen(
                    uiState = uiState,
                    onSessionIntent = {},
                    onFinishWorkout = {}
                )
            }
        }

        composeTestRule.onNodeWithText("WORKOUT COMPLETE!").assertIsDisplayed()
        composeTestRule.onNodeWithText(plan.title, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("TOTAL DURATION", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("TOTAL VOLUME", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("SETS COMPLETED", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("TOTAL REPETITIONS", substring = true)
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun selectingRpeAndEnteringNotesTriggersIntentsAndFinishTriggersCallback() {
        val plan = TRAINING_PLAN
        val statistics =
            TrainingStatistics(
                trainingPlanId = plan.id,
                totalTime = 1800.seconds,
                date = Date(0L),
                exercisesStatistics = emptyList()
            )

        val domainState =
            TrainingSessionState.SummaryState(
                statistics = statistics,
                selectedOverallRpe = null,
                notes = ""
            )
        val uiState =
            TrainingSessionUiState.Summary(
                domainState = domainState,
                trainingPlan = ResultHandler.Success(plan)
            )

        val recordedIntents = mutableListOf<SessionIntent>()
        var finishClicked = false

        composeTestRule.setContent {
            TrainingPlannerTheme {
                TrainingSessionSummaryScreen(
                    uiState = uiState,
                    onSessionIntent = { recordedIntents.add(it) },
                    onFinishWorkout = { finishClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Add observations, fatigue level, or progression goals...")
            .performScrollTo()
            .performTextInput("Hit all targets")

        composeTestRule.onNodeWithText("8").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Finish Workout").performScrollTo().performClick()

        assertEquals(
            SessionIntent.SessionFeedbackSelected(SessionRpe.VERY_HARD),
            recordedIntents.filterIsInstance<SessionIntent.SessionFeedbackSelected>().single()
        )
        assertTrue(
            recordedIntents.any {
                it is SessionIntent.SessionNotesChanged && it.notes == "Hit all targets"
            }
        )
        assertTrue(finishClicked)
    }
}
