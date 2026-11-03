package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState.ExerciseState
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TrainingExerciseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysRepBasedExerciseAndCompletesSet() {
        val exercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = createDummyExercise("Bench Press"),
            sets = 3,
            repetitions = 10,
            weightInKg = 60,
            restTime = 60.seconds
        )
        val state = ExerciseState(
            currentExercise = exercise,
            currentSet = 1,
            totalSets = 3,
            exerciseIndex = 1,
            totalExercises = 4,
            followingExerciseName = "Incline Dumbbell Press"
        )

        var completedReps: Int? = null
        var completedWeight: Double? = null
        var completedTime: Duration? = null

        composeTestRule.setContent {
            TrainingPlannerTheme2 {
                TrainingExerciseScreen(
                    state = state,
                    onCompleteSet = { reps, weight, time ->
                        completedReps = reps
                        completedWeight = weight
                        completedTime = time
                    },
                    onSkipSet = {},
                    onSkipExercise = {},
                    onAddExtraSet = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACTIVE SET 1").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Complete Set"
        ).performScrollTo().assertIsDisplayed().performClick()

        assertEquals(10, completedReps)
        assertEquals(60.0, completedWeight)
        assertEquals(null, completedTime)
    }

    @Test
    fun displaysIsometricExerciseAndCompletesSet() {
        val exercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = createDummyExercise("Plank"),
            sets = 3,
            repetitions = 1,
            time = 45.seconds,
            restTime = 30.seconds
        )
        val state = ExerciseState(
            currentExercise = exercise,
            currentSet = 2,
            totalSets = 3,
            exerciseIndex = 2,
            totalExercises = 4,
            followingExerciseName = null
        )

        var completedReps: Int? = null
        var completedWeight: Double? = null
        var completedTime: Duration? = null

        composeTestRule.setContent {
            TrainingPlannerTheme2 {
                TrainingExerciseScreen(
                    state = state,
                    onCompleteSet = { reps, weight, time ->
                        completedReps = reps
                        completedWeight = weight
                        completedTime = time
                    },
                    onSkipSet = {},
                    onSkipExercise = {},
                    onAddExtraSet = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Plank").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACTIVE SET 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Target: 45s hold").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Complete Set"
        ).performScrollTo().assertIsDisplayed().performClick()

        assertEquals(1, completedReps)
        assertEquals(null, completedWeight)
        assertEquals(45.seconds, completedTime)
    }

    @Test
    fun triggersSkipSetAndSecondaryActions() {
        val exercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = createDummyExercise("Pull-ups"),
            sets = 4,
            repetitions = 8,
            restTime = 90.seconds
        )
        val state = ExerciseState(
            currentExercise = exercise,
            currentSet = 1,
            totalSets = 4,
            exerciseIndex = 1,
            totalExercises = 3,
            followingExerciseName = null
        )

        var skipSetClicked = false
        var skipExerciseClicked = false
        var addExtraSetClicked = false

        composeTestRule.setContent {
            TrainingPlannerTheme2 {
                TrainingExerciseScreen(
                    state = state,
                    onCompleteSet = { _, _, _ -> },
                    onSkipSet = { skipSetClicked = true },
                    onSkipExercise = { skipExerciseClicked = true },
                    onAddExtraSet = { addExtraSetClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Skip Set").performScrollTo().performClick()
        assertTrue(skipSetClicked)

        composeTestRule.onNodeWithText("Skip Exercise").performScrollTo().performClick()
        assertTrue(skipExerciseClicked)

        composeTestRule.onNodeWithText("Add Extra Set").performScrollTo().performClick()
        assertTrue(addExtraSetClicked)
    }
}
