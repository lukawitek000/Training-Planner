package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState.RestTimeState
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RestTimeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysRestIntervalWithTimerAndUpNextExercise() {
        val nextExercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = createDummyExercise("Barbell Squat"),
            sets = 4,
            repetitions = 8,
            weightInKg = 100,
            restTime = 90.seconds
        )
        val state = RestTimeState(
            nextExercise = nextExercise,
            restTime = Time(90000L),
            nextSet = 2,
            totalSetsForNextExercise = 4,
            followingExerciseName = "Romanian Deadlift"
        )

        composeTestRule.setContent {
            TrainingPlannerTheme2 {
                RestTimeScreen(
                    state = state,
                    timeLeft = Time(45000L),
                    totalTime = Time(90000L),
                    onStartNextSet = {},
                    onSkipRest = {}
                )
            }
        }

        composeTestRule.onNodeWithText("REST INTERVAL").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "HOW DID THAT SET FEEL?"
        ).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("UP NEXT").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Barbell Squat").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Set 2 of 4").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("8 reps • 100 kg").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Following: Romanian Deadlift"
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun allowsSelectingFeedbackChip() {
        val nextExercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = createDummyExercise("Overhead Press"),
            sets = 3,
            repetitions = 6,
            restTime = 60.seconds
        )
        val state = RestTimeState(
            nextExercise = nextExercise,
            restTime = Time(60000L),
            nextSet = 1,
            totalSetsForNextExercise = 3
        )

        composeTestRule.setContent {
            TrainingPlannerTheme2 {
                RestTimeScreen(
                    state = state,
                    timeLeft = Time(60000L),
                    totalTime = Time(60000L),
                    onStartNextSet = {},
                    onSkipRest = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Good").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Hard").assertIsDisplayed().performClick()
    }

    @Test
    fun triggersStartNextSetAndSkipRestCallbacks() {
        val nextExercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = createDummyExercise("Bench Press"),
            sets = 3,
            repetitions = 10,
            restTime = 60.seconds
        )
        val state = RestTimeState(
            nextExercise = nextExercise,
            restTime = Time(60000L),
            nextSet = 3,
            totalSetsForNextExercise = 3
        )

        var startNextSetClicked = false
        var skipRestClicked = false

        composeTestRule.setContent {
            TrainingPlannerTheme2 {
                RestTimeScreen(
                    state = state,
                    timeLeft = Time(30000L),
                    totalTime = Time(60000L),
                    onStartNextSet = { startNextSetClicked = true },
                    onSkipRest = { skipRestClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Start Next Set Now").performScrollTo().performClick()
        assertTrue(startNextSetClicked)

        composeTestRule.onNodeWithText("Skip Rest").performScrollTo().performClick()
        assertTrue(skipRestClicked)
    }
}
