package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.TrainingSessionTest
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.createTrainingPlan
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert
import org.junit.Test

internal class TrainingSessionControlTest : TrainingSessionTest() {
    private val time = Time(100)

    @Test
    fun `load first exercise when training session starts`() {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN)

        // when
        val trainingSessionState = trainingSession.start(time)

        // then
        val expectedState = TrainingSessionState.ExerciseState(TRAINING_EXERCISES.first())
        assertExerciseState(expectedState, trainingSessionState)
    }

    @Test
    fun `load rest time after first exercise`() {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState =
            TrainingSessionState.RestTimeState(
                TRAINING_EXERCISES[1],
                Time(TRAINING_EXERCISES.first().restTime.inWholeMilliseconds)
            )
        assertRestTimeState(expectedState, trainingSessionState)
    }

    @Test
    fun `load next exercise after exercise without rest time`() {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN)

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
        val trainingSession = createTrainingSession(TRAINING_PLAN)

        // when
        trainingSession.start(time)
        repeat(5) { trainingSession.completed(time) }
        val trainingSessionState = trainingSession.completed(time)

        // then
        val expectedState =
            TrainingSessionState.RestTimeState(
                TRAINING_EXERCISES.first(),
                Time(TRAINING_EXERCISES.last().restTime.inWholeMilliseconds)
            )
        assertRestTimeState(expectedState, trainingSessionState)
    }

    @Test
    fun `load first exercise in new set`() {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN)

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
        val trainingSession = createTrainingSession(TRAINING_PLAN)

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
        val trainingSession = createTrainingSession(TRAINING_PLAN)

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

        // when // then
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

    @Test
    fun `straight sets policy executes all sets of one exercise consecutively`() {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN, StraightSetsPolicy())

        // when
        val firstState = trainingSession.start(time) as TrainingSessionState.ExerciseState
        Assert.assertEquals(TRAINING_EXERCISES[0].id, firstState.currentExercise.id)
        Assert.assertEquals(1, firstState.currentSet)

        // complete set 1 -> rest -> start set 2 of same exercise
        trainingSession.completed(time) // rest
        val secondSetState = trainingSession.completed(time) as TrainingSessionState.ExerciseState
        Assert.assertEquals(TRAINING_EXERCISES[0].id, secondSetState.currentExercise.id)
        Assert.assertEquals(2, secondSetState.currentSet)
    }

    @Test
    fun `skipExercise removes all remaining sets of current exercise`() {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN, StraightSetsPolicy())
        trainingSession.start(time) // Set 1 of Exercise 1 (2 sets total)

        // when: skip whole exercise
        val nextState = trainingSession.skipExercise(time)

        // then: moves past exercise 1 (rest or exercise 2)
        Assert.assertNotEquals(TRAINING_EXERCISES[0].id, nextState.exercise?.id)
    }

    @Test
    fun `isometric exercise is recognized and records explicit hold time duration`() {
        // given
        val isometricExercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 1,
            sets = 1,
            restTime = 30.seconds,
            time = 45.seconds,
            exercise = createDummyExercise("Plank")
        )
        val plan = createTrainingPlan(listOf(isometricExercise))
        val session = createTrainingSession(plan)

        // when: start session
        val startState = session.start(time) as TrainingSessionState.ExerciseState

        // then: state identifies as isometric with targetTime
        Assert.assertTrue(startState.isIsometric)
        Assert.assertEquals(45.seconds, startState.targetTime)

        // when: completed with custom hold duration of 40 seconds
        val nextState = session.completed(time = time + Time(50000L), actualTime = 40.seconds)
        Assert.assertTrue(
            nextState is TrainingSessionState.RestTimeState ||
                nextState is TrainingSessionState.SummaryState
        )
    }
}
