package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

internal open class TrainingSessionTest {

    private val trainingSetsStrategy: TrainingSetsPolicy = CircuitSetsPolicy()

    protected fun createTrainingSession(trainingPlan: TrainingPlan) =
        TrainingSession(trainingPlan, trainingSetsStrategy)

    protected fun createTrainingPlan(trainingExercises: List<TrainingExercise>): TrainingPlan {
        return TrainingPlan(
            id = TrainingPlanId.create(),
            title = "Training Plan Title",
            exercises = trainingExercises
        )
    }

    protected fun createSingleTrainingExerciseWithManySets(): List<TrainingExercise> {
        val exercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 2,
            time = Time(10000L),
            restTime = Time(30000)
        )
        return listOf(exercise)
    }

    protected fun createTrainingExercisesWithDifferentSets(): List<TrainingExercise> {
        val exercise1 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 2,
            time = Time(10000L),
            restTime = Time(30000)
        )
        val exercise2 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 3,
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

    protected fun assertRestTimeState(
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

    protected fun assertExerciseState(
        expectedState: TrainingSessionState,
        trainingSessionState: TrainingSessionState
    ) {
        assertTrue(trainingSessionState is TrainingSessionState.ExerciseState)
        assertEquals(expectedState.exercise, trainingSessionState.exercise)
    }
}