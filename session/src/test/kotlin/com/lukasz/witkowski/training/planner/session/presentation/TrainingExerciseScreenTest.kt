package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TrainingExerciseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
// TODO
//    @Test
//    fun displaysRepBasedExerciseAndCompletesSet() {
//        val exercise = TrainingExercise(
//            id = TrainingExerciseId.create(),
//            exercise = createDummyExercise("Bench Press"),
//            sets = 3,
//            repetitions = 10,
//            weightInKg = 60,
//            restTime = 60.seconds
//        )
//        val state = ExerciseState(
//            currentExercise = exercise,
//            currentSet = 1,
//            totalSets = 3,
//            exerciseIndex = 1,
//            totalExercises = 4,
//            followingExercise = null,
//            selectedReps = 10,
//            selectedWeightInKg = 60.0
//        )
//
//        var completedCalled = false
//
//        composeTestRule.setContent {
//            TrainingPlannerTheme {
//                TrainingExerciseScreen(
//                    state = state,
//                    onRepsChanged = {},
//                    onWeightChanged = {},
//                    onHoldDurationChanged = {},
//                    onToggleHoldTimer = {},
//                    onResetHoldTimer = {},
//                    onCompleteSet = { completedCalled = true },
//                    onSkipSet = {},
//                    onSkipExercise = {},
//                    onAddExtraSet = {}
//                )
//            }
//        }
//
//        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
//        composeTestRule.onNodeWithText("ACTIVE SET 1").assertIsDisplayed()
//        composeTestRule.onNodeWithText(
//            "Complete Set"
//        ).performScrollTo().assertIsDisplayed().performClick()
//
//        assertTrue(completedCalled)
//    }
//
//    @Test
//    fun displaysIsometricExerciseAndCompletesSet() {
//        val exercise = TrainingExercise(
//            id = TrainingExerciseId.create(),
//            exercise = createDummyExercise("Plank"),
//            sets = 3,
//            repetitions = 1,
//            time = 45.seconds,
//            restTime = 30.seconds
//        )
//        val state = ExerciseState(
//            currentExercise = exercise,
//            currentSet = 2,
//            totalSets = 3,
//            exerciseIndex = 2,
//            totalExercises = 4,
//            followingExercise = null,
//            targetTime = 45.seconds,
//            selectedHoldDuration = 45.seconds,
//            remainingHoldTime = 45.seconds
//        )
//
//        var completedCalled = false
//
//        composeTestRule.setContent {
//            TrainingPlannerTheme {
//                TrainingExerciseScreen(
//                    state = state,
//                    onRepsChanged = {},
//                    onWeightChanged = {},
//                    onHoldDurationChanged = {},
//                    onToggleHoldTimer = {},
//                    onResetHoldTimer = {},
//                    onCompleteSet = { completedCalled = true },
//                    onSkipSet = {},
//                    onSkipExercise = {},
//                    onAddExtraSet = {}
//                )
//            }
//        }
//
//        composeTestRule.onNodeWithText("Plank").assertIsDisplayed()
//        composeTestRule.onNodeWithText("ACTIVE SET 2").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Target: 45 s hold").assertIsDisplayed()
//        composeTestRule.onNodeWithText(
//            "Complete Set"
//        ).performScrollTo().assertIsDisplayed().performClick()
//
//        assertTrue(completedCalled)
//    }
//
//    @Test
//    fun triggersSkipSetAndSecondaryActions() {
//        val exercise = TrainingExercise(
//            id = TrainingExerciseId.create(),
//            exercise = createDummyExercise("Pull-ups"),
//            sets = 4,
//            repetitions = 8,
//            restTime = 90.seconds
//        )
//        val state = ExerciseState(
//            currentExercise = exercise,
//            currentSet = 1,
//            totalSets = 4,
//            exerciseIndex = 1,
//            totalExercises = 3,
//            followingExercise = null
//        )
//
//        var skipSetClicked = false
//        var skipExerciseClicked = false
//        var addExtraSetClicked = false
//
//        composeTestRule.setContent {
//            TrainingPlannerTheme {
//                TrainingExerciseScreen(
//                    state = state,
//                    onRepsChanged = {},
//                    onWeightChanged = {},
//                    onHoldDurationChanged = {},
//                    onToggleHoldTimer = {},
//                    onResetHoldTimer = {},
//                    onCompleteSet = {},
//                    onSkipSet = { skipSetClicked = true },
//                    onSkipExercise = { skipExerciseClicked = true },
//                    onAddExtraSet = { addExtraSetClicked = true }
//                )
//            }
//        }
//
//        composeTestRule.onNodeWithText("Skip Set").performScrollTo().performClick()
//        assertTrue(skipSetClicked)
//
//        composeTestRule.onNodeWithText("Skip Exercise").performScrollTo().performClick()
//        assertTrue(skipExerciseClicked)
//
//        composeTestRule.onNodeWithText("Add Extra Set").performScrollTo().performClick()
//        assertTrue(addExtraSetClicked)
//    }
}
