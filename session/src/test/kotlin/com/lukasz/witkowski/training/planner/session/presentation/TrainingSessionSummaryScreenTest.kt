package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.createTrainingPlan
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
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
@Config(sdk = [33])
class TrainingSessionSummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysWorkoutCompletionAndMetrics() {
        val plan = createTrainingPlan(
            trainingExercises = listOf(
                TrainingExercise(
                    id = TrainingExerciseId.create(),
                    exercise = createDummyExercise("Barbell Deadlift"),
                    sets = 3,
                    repetitions = 5,
                    weightInKg = 140,
                    restTime = 120.seconds
                )
            )
        ).copy(title = "Heavy Pull Day")

        val statsId = TrainingStatisticsId.create()
        val exStatsId = ExerciseStatisticsId.create()
        val trainingExerciseId = plan.exercises.first().id
        val statistics = TrainingStatistics(
            id = statsId,
            trainingPlanId = plan.id,
            totalTime = Time(2700000L), // 45m
            date = Date(),
            exercisesStatistics = listOf(
                ExerciseStatistics(
                    id = exStatsId,
                    trainingExerciseId = trainingExerciseId,
                    attemptsStatistics = listOf(
                        ExerciseAttemptStatistics(
                            id = ExerciseAttemptStatisticsId.create(),
                            trainingExerciseId = trainingExerciseId,
                            time = Time(30000L),
                            set = 1,
                            actualReps = 5,
                            actualWeightInKg = 140.0,
                            completed = true
                        )
                    )
                )
            )
        )

        val state = TrainingSessionState.SummaryState(
            statistics = statistics,
            trainingPlan = plan
        )

        composeTestRule.setContent {
            TrainingPlannerTheme {
                TrainingSessionSummaryScreen(
                    state = state,
                    onRpeSelected = {},
                    onNotesChanged = {},
                    onSaveWorkout = {}
                )
            }
        }

        composeTestRule.onNodeWithText("WORKOUT COMPLETE!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Heavy Pull Day", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("TOTAL DURATION").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("TOTAL VOLUME").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("SETS COMPLETED").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("TOTAL REPETITIONS").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun savesWorkoutWithSelectedRpeAndNotes() {
        val plan = createTrainingPlan(
            trainingExercises = listOf(
                TrainingExercise(
                    id = TrainingExerciseId.create(),
                    exercise = createDummyExercise("Bench Press"),
                    sets = 1,
                    repetitions = 10,
                    weightInKg = 80,
                    restTime = 60.seconds
                )
            )
        )

        val statsId = TrainingStatisticsId.create()
        val statistics = TrainingStatistics(
            id = statsId,
            trainingPlanId = plan.id,
            totalTime = Time(1800000L),
            date = Date(),
            exercisesStatistics = emptyList()
        )

        var selectedRpe: SessionRpe? = null
        var notes = ""
        var savedCalled = false

        composeTestRule.setContent {
            TrainingPlannerTheme {
                TrainingSessionSummaryScreen(
                    state = TrainingSessionState.SummaryState(
                        statistics = statistics,
                        trainingPlan = plan,
                        selectedOverallRpe = selectedRpe,
                        notes = notes
                    ),
                    onRpeSelected = { selectedRpe = it },
                    onNotesChanged = { notes = it },
                    onSaveWorkout = { savedCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Add observations, fatigue level, or progression goals...")
            .performScrollTo()
            .performTextInput("Hit target reps with clean form")

        composeTestRule.onNodeWithText("8").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Save & Finish Workout").performScrollTo().performClick()

        assertEquals("Hit target reps with clean form", notes)
        assertEquals(SessionRpe.VERY_HARD, selectedRpe)
        assertTrue(savedCalled)
    }
}
