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
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class TrainingSessionTest {

    private val mockStatisticsRecorder: StatisticsRecorder = mock()
    private val trainingSetsStrategy: TrainingSetsStrategy = CircuitSetsStrategy()

    private val trainingExercises = createTrainingExercisesWithDifferentSets()
    private lateinit var trainingSession: TrainingSession

    @Before
    fun setUp() {
        val trainingPlan = createTrainingPlan(trainingExercises)
        trainingSession = TrainingSession(trainingPlan, mockStatisticsRecorder, trainingSetsStrategy)
    }

    @Test
    fun `load first exercise when training session starts`() {
        // when
        val trainingSessionState = trainingSession.start()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises.first())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load rest time after first exercise`() {
        // when
        trainingSession.start()
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
        // when
        trainingSession.start()
        trainingSession.next()
        trainingSession.next()
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises[2])
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load rest time after last exercise in set`() {
        // when
        trainingSession.start()
        repeat(5) { trainingSession.next() }
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.RestTimeState(trainingExercises.first(), trainingExercises.last().restTime)
        assertRestTimeState(expectedState, trainingSessionState)
    }

    @Test
    fun `load first exercise in new set`() {
        // when
        trainingSession.start()
        repeat(6) { trainingSession.next() }
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises.first())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `skip exercise if all attempts were done`() {
        // when
        trainingSession.start()
        repeat(9) { trainingSession.next() }
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises.last())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load the same exercise if only one has left sets`() {
        // when
        trainingSession.start()
        repeat(14) { trainingSession.next() }
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises.last())
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