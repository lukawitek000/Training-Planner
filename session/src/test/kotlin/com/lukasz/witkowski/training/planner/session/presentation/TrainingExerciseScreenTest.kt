package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.lukasz.witkowski.training.planner.session.createSessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionUiState
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.utils.LocalWeightUnit
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.shared.utils.WeightUnit
import com.lukasz.witkowski.training.planner.shared.utils.kg
import com.lukasz.witkowski.training.planner.shared.utils.lbs
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
class TrainingExerciseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysRepBasedExerciseAndTriggersSetCompleted() {
        val exercise =
            createSessionExercise(
                name = "Bench Press",
                reps = 10,
                weight = 60.kg
            )
        val progress =
            ExerciseQueueProgress(
                remainingExerciseSets = 2,
                currentExerciseTotalSets = 3,
                totalExercises = 4,
                remainingExercises = 4,
                remainingSetsInWorkout = 8,
                nextSetQueueProgress = null
            )
        val state =
            TrainingSessionState.ExerciseState(
                sessionExercise = exercise,
                timerState = TimerState.NotSet,
                progress = progress
            )
        val uiState =
            TrainingSessionUiState.Exercise(
                domainState = state,
                imagesReferences = ResultHandler.Success(emptyList())
            )

        val recordedIntents = mutableListOf<SessionIntent>()

        composeTestRule.setContent {
            TrainingPlannerTheme {
                TrainingExerciseScreen(
                    uiState = uiState,
                    onSessionIntent = { recordedIntents.add(it) },
                    onEndClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACTIVE SET 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Complete Set").performClick()

        assertTrue(recordedIntents.contains(SessionIntent.SetCompleted))
    }

    @Test
    fun triggersSecondaryIntentsLikeSkipSetAndSetModifications() {
        val exercise = createSessionExercise(name = "Pull-ups")
        val progress =
            ExerciseQueueProgress(
                remainingExerciseSets = 1,
                currentExerciseTotalSets = 2,
                totalExercises = 2,
                remainingExercises = 2,
                remainingSetsInWorkout = 3,
                nextSetQueueProgress = null
            )
        val state =
            TrainingSessionState.ExerciseState(
                sessionExercise = exercise,
                timerState = TimerState.NotSet,
                progress = progress
            )
        val uiState =
            TrainingSessionUiState.Exercise(
                domainState = state,
                imagesReferences = ResultHandler.Success(emptyList())
            )

        val recordedIntents = mutableListOf<SessionIntent>()

        composeTestRule.setContent {
            TrainingPlannerTheme {
                TrainingExerciseScreen(
                    uiState = uiState,
                    onSessionIntent = { recordedIntents.add(it) },
                    onEndClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Skip Set").performClick()
        assertEquals(SessionIntent.SetSkipped, recordedIntents.last())

        composeTestRule.onNodeWithText("Add Extra Set").performScrollTo().performClick()
        assertEquals(SessionIntent.SetAdded, recordedIntents.last())

        composeTestRule.onNodeWithText("Remove Set").performScrollTo().performClick()
        assertEquals(SessionIntent.SetRemoved, recordedIntents.last())
    }

    @Test
    fun displaysIsometricExerciseAndTriggersStartTimer() {
        val exercise =
            createSessionExercise(
                name = "Plank",
                holdTime = 45.seconds
            )
        val progress =
            ExerciseQueueProgress(
                remainingExerciseSets = 0,
                currentExerciseTotalSets = 1,
                totalExercises = 1,
                remainingExercises = 1,
                remainingSetsInWorkout = 1,
                nextSetQueueProgress = null
            )
        val state =
            TrainingSessionState.ExerciseState(
                sessionExercise = exercise,
                timerState = TimerState.Ready(45.seconds),
                progress = progress
            )
        val uiState =
            TrainingSessionUiState.Exercise(
                domainState = state,
                imagesReferences = ResultHandler.Success(emptyList())
            )

        val recordedIntents = mutableListOf<SessionIntent>()

        composeTestRule.setContent {
            TrainingPlannerTheme {
                TrainingExerciseScreen(
                    uiState = uiState,
                    onSessionIntent = { recordedIntents.add(it) },
                    onEndClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Plank").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Start Hold Timer")
            .performScrollTo()
            .performClick()

        assertTrue(recordedIntents.contains(SessionIntent.StartTimer))
    }

    @Test
    fun displaysWeightInLbsWhenLocalWeightUnitIsLbs() {
        val exercise = createSessionExercise(
            name = "Bench Press",
            reps = 10,
            weight = 60.kg
        )
        val progress = ExerciseQueueProgress(
            remainingExerciseSets = 2,
            currentExerciseTotalSets = 3,
            totalExercises = 1,
            remainingExercises = 1,
            remainingSetsInWorkout = 3,
            nextSetQueueProgress = null
        )
        val state = TrainingSessionState.ExerciseState(
            sessionExercise = exercise,
            timerState = TimerState.NotSet,
            progress = progress
        )
        val uiState = TrainingSessionUiState.Exercise(
            domainState = state,
            imagesReferences = ResultHandler.Success(emptyList())
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalWeightUnit provides WeightUnit.LBS) {
                TrainingPlannerTheme {
                    TrainingExerciseScreen(
                        uiState = uiState,
                        onSessionIntent = {},
                        onEndClicked = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("132.28").assertIsDisplayed()
        composeTestRule.onNodeWithText("lbs").assertIsDisplayed()
    }

    @Test
    fun incrementsWeightByFiveLbsWhenLocalWeightUnitIsLbs() {
        val exercise = createSessionExercise(
            name = "Bench Press",
            reps = 10,
            weight = 100.lbs
        )
        val progress = ExerciseQueueProgress(
            remainingExerciseSets = 2,
            currentExerciseTotalSets = 3,
            totalExercises = 1,
            remainingExercises = 1,
            remainingSetsInWorkout = 3,
            nextSetQueueProgress = null
        )
        val state = TrainingSessionState.ExerciseState(
            sessionExercise = exercise,
            timerState = TimerState.NotSet,
            progress = progress
        )
        val uiState = TrainingSessionUiState.Exercise(
            domainState = state,
            imagesReferences = ResultHandler.Success(emptyList())
        )
        val recordedIntents = mutableListOf<SessionIntent>()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalWeightUnit provides WeightUnit.LBS) {
                TrainingPlannerTheme {
                    TrainingExerciseScreen(
                        uiState = uiState,
                        onSessionIntent = { recordedIntents.add(it) },
                        onEndClicked = {}
                    )
                }
            }
        }

        composeTestRule.onNode(
            hasContentDescription("Increase") and
                hasAnySibling(hasText("lbs", substring = true))
        ).performClick()

        val weightIntent = recordedIntents.filterIsInstance<SessionIntent.WeightChanged>().last()
        assertEquals(105.0, weightIntent.weight.inPounds, 1e-6)
    }
}
