package com.lukasz.witkowski.training.planner.session

import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

internal open class TrainingSessionTest {
    private val trainingSetsStrategy: TrainingSetsPolicy = CircuitSetsPolicy()

    protected fun createTrainingSession(
        trainingPlan: TrainingPlan,
        policy: TrainingSetsPolicy = trainingSetsStrategy
    ) = TrainingSession(trainingPlan, policy)

    protected fun createSingleTrainingExerciseWithManySets(): List<TrainingExercise> {
        val exercise =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                repetitions = 10,
                sets = 2,
                restTime = 30.seconds,
                exercise = createDummyExercise()
            )
        return listOf(exercise)
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
