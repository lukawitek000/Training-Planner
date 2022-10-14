package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsStrategy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsStrategy
import com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.StatisticsRecorder
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertFailsWith

class TrainingSessionTest {

    private val mockStatisticsRecorder: StatisticsRecorder = mock()
    private val trainingSetsStrategy: TrainingSetsStrategy = CircuitSetsStrategy()

    @Test
    fun `load first exercise when training session starts`() {
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession =
            createTrainingSession(trainingPlan)

        // when
        val trainingSessionState = trainingSession.start()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises.first())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load rest time after first exercise`() {
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession =
            createTrainingSession(trainingPlan)

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
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession =
            createTrainingSession(trainingPlan)

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
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession =
            createTrainingSession(trainingPlan)

        // when
        trainingSession.start()
        repeat(5) { trainingSession.next() }
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.RestTimeState(
            trainingExercises.first(),
            trainingExercises.last().restTime
        )
        assertRestTimeState(expectedState, trainingSessionState)
    }

    @Test
    fun `load first exercise in new set`() {
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession =
            createTrainingSession(trainingPlan)

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
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession =
            createTrainingSession(trainingPlan)

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
        // given
        val trainingExercises = createTrainingExercisesWithDifferentSets()
        val trainingPlan = createTrainingPlan(trainingExercises)
        val trainingSession =
            createTrainingSession(trainingPlan)

        // when
        trainingSession.start()
        repeat(14) { trainingSession.next() }
        val trainingSessionState = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercises.last())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `training plan with empty exercises list is rejected`() {
        // given
        val trainingPlan = createTrainingPlan(emptyList())

        // when
        // then
        assertFailsWith<IllegalArgumentException> { createTrainingSession(trainingPlan) }
    }

    @Test
    fun `training session with single exercise with many sets`() {
        // given
        val trainingExercise = createSingleTrainingExerciseWithManySets()
        val trainingPlan = createTrainingPlan(trainingExercise)
        val trainingSession = createTrainingSession(trainingPlan)

        // when
        trainingSession.start()
        trainingSession.next()
        val state = trainingSession.next()

        // then
        val expectedState = TrainingSessionState.ExerciseState(trainingExercise.first())
        assertExerciseState(expectedState, state)
    }

    @Test
    fun `training session ended with summary state`() {
        // given
        val trainingExercise = createSingleTrainingExerciseWithManySets()
        val trainingPlan = createTrainingPlan(trainingExercise)
        val timeProvider = FixedTimeProvider()
        val trainingStatistics = TrainingStatistics(TrainingStatisticsId.create(), trainingPlan.id, timeProvider.currentTime(), timeProvider.currentDate(), emptyList())
        doReturn(trainingStatistics).`when`(mockStatisticsRecorder).stop()
        val trainingSession = createTrainingSession(trainingPlan)

        // when
        trainingSession.start()
        trainingSession.next()
        trainingSession.next()
        val state = trainingSession.next()

        // then
        assertTrue(state is TrainingSessionState.SummaryState)
    }

    private fun createTrainingSession(trainingPlan: TrainingPlan) =
        TrainingSession(trainingPlan, mockStatisticsRecorder, trainingSetsStrategy)

    private fun createTrainingPlan(trainingExercises: List<TrainingExercise>): TrainingPlan {
        return TrainingPlan(
            id = TrainingPlanId.create(),
            title = "Training Plan Title",
            exercises = trainingExercises
        )
    }

    private fun createSingleTrainingExerciseWithManySets(): List<TrainingExercise> {
        val exercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 2,
            time = Time(10000L),
            restTime = Time(30000),
            exercise = createExercise()
        )
        return listOf(exercise)
    }

    private fun createTrainingExercisesWithDifferentSets(): List<TrainingExercise> {
        val exercise1 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 2,
            time = Time(10000L),
            restTime = Time(30000),
            exercise = createExercise()
        )
        val exercise2 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 3,
            time = Time(10000L),
            restTime = Time.NONE,
            exercise = createExercise()
        )
        val exercise3 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 1,
            time = Time(10000L),
            restTime = Time(30000),
            exercise = createExercise()
        )
        val exercise4 = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 5,
            time = Time(10000L),
            restTime = Time(30000),
            exercise = createExercise()
        )
        return listOf(exercise1, exercise2, exercise3, exercise4)
    }

    private fun createExercise() = Exercise(
        ExerciseId.create(), "", ""
    )

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