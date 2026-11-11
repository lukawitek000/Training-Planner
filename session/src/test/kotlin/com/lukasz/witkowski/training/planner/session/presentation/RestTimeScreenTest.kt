package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lukasz.witkowski.training.planner.session.createSessionExercise
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
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
class RestTimeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysRestIntervalWithFeedbackAndUpNextExercise() {
        val nextExercise =
            createSessionExercise(
                name = "Barbell Squat",
                reps = 8,
                weightInKg = 100.0
            )
        val nextProgress =
            ExerciseQueueProgress(
                remainingExerciseSets = 2,
                currentExerciseTotalSets = 4,
                totalExercises = 3,
                remainingExercises = 3,
                remainingSetsInWorkout = 6,
                nextSetQueueProgress = null
            )
        val state =
            TrainingSessionState.RestTimeState(
                nextExercise = nextExercise,
                timerState = TimerState.Running(45.seconds),
                totalRestTime = 90.seconds,
                nextSetQueueProgress = nextProgress
            )

        composeTestRule.setContent {
            TrainingPlannerTheme {
                RestTimeScreen(
                    state = state,
                    onSessionIntent = {},
                    onEndClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("REST INTERVAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("HOW DID THAT SET FEEL?").assertIsDisplayed()
        composeTestRule.onNodeWithText("UP NEXT").assertExists()
        composeTestRule.onNodeWithText("Barbell Squat").assertExists()
        composeTestRule.onNodeWithText("Set 2 of 4").assertExists()
    }

    @Test
    fun selectingFeedbackChipTriggersSetFeedbackSelectedIntent() {
        val nextExercise = createSessionExercise(name = "Overhead Press")
        val state =
            TrainingSessionState.RestTimeState(
                nextExercise = nextExercise,
                timerState = TimerState.Running(30.seconds),
                totalRestTime = 60.seconds,
                nextSetQueueProgress = null
            )

        val recordedIntents = mutableListOf<SessionIntent>()

        composeTestRule.setContent {
            TrainingPlannerTheme {
                RestTimeScreen(
                    state = state,
                    onSessionIntent = { recordedIntents.add(it) },
                    onEndClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Good").assertIsDisplayed().performClick()

        assertEquals(
            SessionIntent.SetFeedbackSelected(SetEffort.MODERATE),
            recordedIntents.last()
        )
    }

    @Test
    fun clickingStartNextSetTriggersRestSkippedIntent() {
        val nextExercise = createSessionExercise(name = "Overhead Press")
        val state =
            TrainingSessionState.RestTimeState(
                nextExercise = nextExercise,
                timerState = TimerState.Running(30.seconds),
                totalRestTime = 60.seconds,
                nextSetQueueProgress = null
            )

        val recordedIntents = mutableListOf<SessionIntent>()

        composeTestRule.setContent {
            TrainingPlannerTheme {
                RestTimeScreen(
                    state = state,
                    onSessionIntent = { recordedIntents.add(it) },
                    onEndClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Start Next Set Now").performClick()

        assertTrue(recordedIntents.contains(SessionIntent.RestSkipped))
    }
}
