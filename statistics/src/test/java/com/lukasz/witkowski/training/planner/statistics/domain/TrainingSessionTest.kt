package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingSessionTest {

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
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load rest time after first exercise`() {
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession = TrainingSession(trainingPlan)
        trainingSession.start()

        // when
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.RestTimeState(
            trainingExercises[1],
            trainingExercises.first().restTime
        )
        assertRestTimeState(expectedState, trainingSessionState)
    }

    @Test
    fun `load next exercise after exercise without rest time`() {
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession = TrainingSession(trainingPlan)
        trainingSession.start()
        trainingSession.next()
        trainingSession.next()
        // TODO how to test above scenarios, that requires iterations

        // when
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises[2])
        assertExerciseState(expectedState, trainingSessionState)
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
            restTime = Time.NONE
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

    private fun assertRestTimeState(
        expectedState: TrainingSessionState,
        trainingSessionState: TrainingSessionState
    ) {
        assertTrue(trainingSessionState is TrainingSessionState.RestTimeState)
        assertEquals(
            (expectedState as TrainingSessionState.RestTimeState).restTime.timeInMillis,
            (trainingSessionState as TrainingSessionState.RestTimeState).restTime.timeInMillis
        )
        assertEquals(expectedState.exercise, trainingSessionState.exercise)
    }

    private fun assertExerciseState(
        expectedState: TrainingSessionState,
        trainingSessionState: TrainingSessionState
    ) {
        assertTrue(trainingSessionState is TrainingSessionState.ExerciseState)
        assertEquals(expectedState.exercise, trainingSessionState.exercise)
    }
}