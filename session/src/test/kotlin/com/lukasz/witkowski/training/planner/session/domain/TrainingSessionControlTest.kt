package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.TrainingSessionTest
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.createTrainingPlan
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrainingSessionControlTest : TrainingSessionTest() {

    @Test
    fun `load first exercise when training session starts`() = runTest(UnconfinedTestDispatcher()) {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)

        // when
        trainingSession.start()

        // then
        assertExerciseState(TRAINING_EXERCISES.first().id, trainingSession.state.value)
    }

    @Test
    fun `load rest time after first exercise set`() = runTest(UnconfinedTestDispatcher()) {
        // given
        val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)

        // when
        trainingSession.start()
        trainingSession.complete()

        // then: StraightSetsPolicy queues Set 2 of Exercise 1 next, with Exercise 1's restTime
        assertRestTimeState(
            expectedNextExerciseId = TRAINING_EXERCISES.first().id,
            expectedTotalRestTime = TRAINING_EXERCISES.first().restTime,
            trainingSessionState = trainingSession.state.value
        )
    }

    @Test
    fun `load next exercise set immediately when restTime is zero`() =
        runTest(UnconfinedTestDispatcher()) {
            // given: Exercise 2 has 3 sets and restTime = 0s
            val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)

            // when: complete Exercise 1 (2 sets + 2 rests) -> arrives at Exercise 2 Set 1
            trainingSession.start()
            repeat(4) { trainingSession.complete() }
            assertExerciseState(TRAINING_EXERCISES[1].id, trainingSession.state.value)

            // complete Set 1 of Exercise 2 (restTime = 0s)
            trainingSession.complete()

            // then: skips RestTimeState and moves straight to Set 2 of Exercise 2
            val state = trainingSession.state.value as TrainingSessionState.ExerciseState
            assertEquals(TRAINING_EXERCISES[1].id, state.sessionExercise.exerciseId)
            assertEquals(1, state.progress.remainingExerciseSets)
            assertEquals(3, state.progress.currentExerciseTotalSets)
        }

    @Test
    fun `load plan rest time after last set of an exercise`() =
        runTest(UnconfinedTestDispatcher()) {
            // given: Exercise 2 has restTime = 0s between its sets, but TRAINING_PLAN has restTime = 30s
            val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)

            // when: complete Exercise 1 (4 steps) + all 3 sets of Exercise 2 (3 steps)
            trainingSession.start()
            repeat(7) { trainingSession.complete() }

            // then: after Set 3 (last set) of Exercise 2, RestTimeState uses TRAINING_PLAN.restTime
            assertRestTimeState(
                expectedNextExerciseId = TRAINING_EXERCISES[2].id,
                expectedTotalRestTime = TRAINING_PLAN.restTime,
                trainingSessionState = trainingSession.state.value
            )
        }

    @Test
    fun `training plan with empty exercises list is rejected`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val trainingPlan = createTrainingPlan(emptyList())

            // when // then
            assertFailsWith<IllegalArgumentException> {
                createTrainingSession(trainingPlan, backgroundScope)
            }
        }

    @Test
    fun `training session with single exercise with many sets`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val trainingExercise = createSingleTrainingExerciseWithManySets()
            val trainingPlan = createTrainingPlan(trainingExercise)
            val trainingSession = createTrainingSession(trainingPlan, backgroundScope)

            // when
            trainingSession.start()
            trainingSession.complete() // Set 1 -> Rest
            trainingSession.complete() // Rest -> Set 2

            // then
            assertExerciseState(trainingExercise.first().id, trainingSession.state.value)
        }

    @Test
    fun `training session ended with summary state after all sets completed`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val trainingExercise = createSingleTrainingExerciseWithManySets()
            val trainingPlan = createTrainingPlan(trainingExercise)
            val trainingSession = createTrainingSession(trainingPlan, backgroundScope)

            // when
            trainingSession.start()
            trainingSession.complete() // Set 1
            trainingSession.complete() // Rest
            trainingSession.complete() // Set 2 (final set)

            // then
            assertTrue(trainingSession.state.value is TrainingSessionState.SummaryState)
        }

    @Test
    fun `rest state automatically transitions to next exercise when rest timer finishes`() =
        runTest(UnconfinedTestDispatcher()) {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val timeProvider = FixedTimeProvider()
            val timerFactory =
                TimerFactory(
                    timeProvider = timeProvider,
                    tickDelay = 1.seconds,
                    timerDispatcher = dispatcher
                )
            val trainingExercise = createSingleTrainingExerciseWithManySets() // 30s rest
            val trainingPlan = createTrainingPlan(trainingExercise)
            val trainingSession =
                createTrainingSession(
                    trainingPlan = trainingPlan,
                    scope = backgroundScope,
                    timeProvider = timeProvider,
                    timerFactory = timerFactory
                )

            trainingSession.start()
            trainingSession.complete()

            assertTrue(trainingSession.state.value is TrainingSessionState.RestTimeState)

            // Advance 30s on monotonic clock and scheduler
            timeProvider.advanceBy(30.seconds)
            testScheduler.advanceTimeBy(1000L)
            testScheduler.runCurrent()

            assertExerciseState(trainingExercise.first().id, trainingSession.state.value)
        }

    @Test
    fun `addExtraSet and removeExtraSet dynamically adjust queue progress`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)
            trainingSession.start() // Set 1 of Exercise 1 (2 sets total, 1 remaining in queue)

            // when: add extra set
            trainingSession.addExtraSet()
            val afterAdd = trainingSession.state.value as TrainingSessionState.ExerciseState
            assertEquals(3, afterAdd.progress.currentExerciseTotalSets)
            assertEquals(2, afterAdd.progress.remainingExerciseSets)

            // when: remove extra set
            trainingSession.removeExtraSet()
            val afterRemove = trainingSession.state.value as TrainingSessionState.ExerciseState
            assertEquals(2, afterRemove.progress.currentExerciseTotalSets)
            assertEquals(1, afterRemove.progress.remainingExerciseSets)
        }

    @Test
    fun `updating exercise reps and weight carries over to remaining sets of same exercise`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)
            trainingSession.start()

            // when: modify reps and weight in Set 1, then complete Set 1 and Rest
            trainingSession.handle(SessionCommand.Exercise.UpdateReps(12))
            trainingSession.handle(SessionCommand.Exercise.UpdateWeight(82.5))
            trainingSession.complete() // Set 1 -> Rest
            trainingSession.complete() // Rest -> Set 2

            // then: Set 2 inherits updated reps & weight while preserving plannedConfig
            val set2State = trainingSession.state.value as TrainingSessionState.ExerciseState
            assertEquals(12, set2State.sessionExercise.currentConfig.reps)
            assertEquals(82.5, set2State.sessionExercise.currentConfig.weightInKg!!, 0.01)
            assertEquals(10, set2State.sessionExercise.snapshot.plannedConfig.reps)
        }

    @Test
    fun `isometric exercise initializes Ready timer and updates hold duration`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val isometricExercise =
                TrainingExercise(
                    id = TrainingExerciseId.create(),
                    repetitions = 1,
                    sets = 1,
                    restTime = 30.seconds,
                    time = 45.seconds,
                    exercise = createDummyExercise("Plank")
                )
            val plan = createTrainingPlan(listOf(isometricExercise))
            val session = createTrainingSession(plan, backgroundScope)

            // when: start session
            session.start()
            val startState = session.state.value as TrainingSessionState.ExerciseState

            // then: timer is Ready with 45s
            assertEquals(TimerState.Ready(45.seconds), startState.timerState)

            // when: update hold duration to 40s and complete
            session.handle(SessionCommand.Exercise.UpdateHoldDuration(40.seconds))
            session.complete()

            val summaryState = session.state.value as TrainingSessionState.SummaryState
            val attempt =
                summaryState.statistics.exercisesStatistics
                    .first()
                    .attemptsStatistics
                    .first()
            assertEquals(40.seconds, attempt.actualTime)
        }

    @Test
    fun `endEarly during ExercisePhase records active set as skipped and goes to SummaryState`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)
            trainingSession.start()
            trainingSession.complete() // Set 1 completed -> Rest
            trainingSession.complete() // Rest completed -> Set 2 started

            // when: end early during Set 2 (ExercisePhase)
            trainingSession.endEarly()

            // then: transitions to SummaryState with Set 1 completed and Set 2 skipped
            val summaryState = trainingSession.state.value as TrainingSessionState.SummaryState
            val attempts =
                summaryState.statistics.exercisesStatistics
                    .first()
                    .attemptsStatistics
            assertEquals(2, attempts.size)
            assertTrue(attempts[0].completed)
            assertEquals(false, attempts[1].completed)
        }

    @Test
    fun `endEarly during RestPhase records rest as completed with RPE and goes to SummaryState`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val trainingSession = createTrainingSession(TRAINING_PLAN, backgroundScope)
            trainingSession.start()
            trainingSession.complete() // Set 1 completed -> RestPhase

            // when: select feedback during RestPhase and end early
            trainingSession.handle(SessionCommand.RestTime.UpdateFeedback(SetEffort.HARD))
            trainingSession.endEarly()

            // then: transitions to SummaryState and Set 1 retains effort = SetEffort.HARD
            val summaryState = trainingSession.state.value as TrainingSessionState.SummaryState
            val attempt =
                summaryState.statistics.exercisesStatistics
                    .first()
                    .attemptsStatistics
                    .first()
            assertTrue(attempt.completed)
            assertEquals(SetEffort.HARD, attempt.effort)
        }
}
