package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.statistics.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.statistics.TrainingSessionTest
import com.lukasz.witkowski.training.planner.statistics.createTrainingPlan
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder.FixedTimeProvider
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFailsWith

internal class TrainingSessionControlTest : TrainingSessionTest() {

    private val time = Time(100)

    @Test
    fun `load first exercise when training session starts`() {
        // given
        val trainingSession =
            createTrainingSession(TRAINING_PLAN)

        // when
        val trainingSessionState = trainingSession.start(time)

        // then
        val expectedState = TrainingSessionState.ExerciseState(TRAINING_EXERCISES.first())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load rest time after first exercise`() {
        // given
        val trainingSession =
            createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState = TrainingSessionState.RestTimeState(
            TRAINING_EXERCISES[1],
            TRAINING_EXERCISES.first().restTime
        )
        assertRestTimeState(expectedState, trainingSessionState)
    }

    @Test
    fun `load next exercise after exercise without rest time`() {
        // given
        val trainingSession =
            createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        trainingSession.completed(time)
        trainingSession.completed(time)
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState = TrainingSessionState.ExerciseState(TRAINING_EXERCISES[2])
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load rest time after last exercise in set`() {
        // given
        val trainingSession =
            createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        repeat(5) { trainingSession.completed(time) }
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState = TrainingSessionState.RestTimeState(
            TRAINING_EXERCISES.first(),
            TRAINING_EXERCISES.last().restTime
        )
        assertRestTimeState(expectedState, trainingSessionState)
    }

    @Test
    fun `load first exercise in new set`() {
        // given
        val trainingSession =
            createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        repeat(6) { trainingSession.completed(time) }
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState = TrainingSessionState.ExerciseState(TRAINING_EXERCISES.first())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `skip exercise if all attempts were done`() {
        // given
        val trainingSession =
            createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        repeat(9) { trainingSession.completed(time) }
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState = TrainingSessionState.ExerciseState(TRAINING_EXERCISES.last())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load the same exercise if only one has left sets`() {
        // given
        val trainingSession =
            createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        repeat(14) { trainingSession.completed(time) }
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState = TrainingSessionState.ExerciseState(TRAINING_EXERCISES.last())
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
        trainingSession.start(time)
        trainingSession.completed(time)
        val state = trainingSession.completed(time)

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
        val trainingSession = createTrainingSession(trainingPlan)

        // when
        val time = timeProvider.currentTime()
        trainingSession.start(time)
        trainingSession.completed(time)
        trainingSession.completed(time)
        val state = trainingSession.completed(time)

        // then
        Assert.assertTrue(state is TrainingSessionState.SummaryState)
    }
}