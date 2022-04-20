package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrainingSessionTest {

    @Before
    fun setUp() {
    }

    @Test
    fun `load first exercise when training session starts`() {
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession = TrainingSession(trainingPlan)

        // when
        val trainingSessionState = trainingSession.start()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingPlan.exercises.first())
        assertTrainingSessionState(expectedState, trainingSessionState)
    }

    private fun assertTrainingSessionState(
        expectedState: TrainingSessionState,
        trainingSessionState: TrainingSessionState
    ) {
        assertTrue(trainingSessionState is TrainingSessionState.ExerciseState)
        assertEquals(expectedState.exercise, trainingSessionState.exercise)
    }


    @Test
    fun `next`() {
        // given


        // when


        // then
    }

    private fun createTrainingPlan(trainingExercises: List<TrainingExercise>): TrainingPlan {
        return TrainingPlan(
            id = TrainingPlanId.create(),
            title = "Training Plan Title",
            exercises = trainingExercises
        )
    }

    private fun createTrainingExercisesWithDifferentSets(): List<TrainingExercise> {
        val exercise1 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 3,
            time = Time(10000L),
            restTime = Time(30000)
        )
        val exercise2 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 2,
            time = Time(10000L),
            restTime = Time(30000)
        )
        val exercise3 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 1,
            time = Time(10000L),
            restTime = Time(30000)
        )
        val exercise4 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 5,
            time = Time(10000L),
            restTime = Time(30000)
        )
        return listOf(exercise1, exercise2, exercise3, exercise4)
    }
}