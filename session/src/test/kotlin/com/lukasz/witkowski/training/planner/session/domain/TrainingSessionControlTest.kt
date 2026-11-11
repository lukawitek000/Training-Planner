package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.TrainingSessionTest
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.createTrainingExercise
import com.lukasz.witkowski.training.planner.session.createTrainingPlan
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrainingSessionControlTest : TrainingSessionTest() {

    @Test
    fun `initial state before start is IdleState and actions are no-ops`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val events = mutableListOf<SessionEvent>()
            val trainingSession =
                createTrainingSession(
                    trainingPlan = TRAINING_PLAN,
                    scope = backgroundScope,
                    statisticsRecorder = SessionStatisticsRecorder { events.add(it) }
                )

            // when
            trainingSession.complete()
            trainingSession.skip()
            trainingSession.endEarly()
            trainingSession.handle(SessionCommand.Exercise.UpdateReps(15))

            // then
            assertEquals(TrainingSessionState.IdleState, trainingSession.state.value)
            assertTrue(events.isEmpty())
        }

    @Test
    fun `calling start when already active is a no-op`() = runTest(UnconfinedTestDispatcher()) {
        // given
        val events = mutableListOf<SessionEvent>()
        val trainingSession =
            createTrainingSession(
                trainingPlan = TRAINING_PLAN,
                scope = backgroundScope,
                statisticsRecorder = SessionStatisticsRecorder { events.add(it) }
            )

        // when
        trainingSession.start()
        val initialExerciseState = trainingSession.state.value
        val initialEventsCount = events.size

        trainingSession.start()

        // then
        assertEquals(initialExerciseState, trainingSession.state.value)
        assertEquals(initialEventsCount, events.size)
    }

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
            assertEquals(2, state.progress.currentExerciseSet)
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
    fun `session ends with summary after all sets and stays in summary on complete or skip`() =
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

            // when: complete and skip in SummaryState
            trainingSession.complete()
            assertTrue(trainingSession.state.value is TrainingSessionState.SummaryState)
            trainingSession.skip()
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
    fun `queue progress tracks exerciseIndex, currentExerciseSet, and nextSetQueueProgress`() =
        runTest(UnconfinedTestDispatcher()) {
            // given: 2 exercises (Ex 1: 2 sets, 30s rest; Ex 2: 2 sets, 30s rest; Plan rest: 45s)
            val ex1 = createTrainingExercise(name = "Ex 1", sets = 2, restTime = 30.seconds)
            val ex2 = createTrainingExercise(name = "Ex 2", sets = 2, restTime = 30.seconds)
            val plan = createTrainingPlan(listOf(ex1, ex2), restTime = 45.seconds)
            val session = createTrainingSession(plan, backgroundScope)

            // 1. Start -> Ex 1, Set 1 of 2
            session.start()
            val ex1Set1 = session.state.value as TrainingSessionState.ExerciseState
            assertEquals(1, ex1Set1.progress.exerciseIndex)
            assertEquals(2, ex1Set1.progress.totalExercises)
            assertEquals(2, ex1Set1.progress.remainingExercises)
            assertEquals(1, ex1Set1.progress.currentExerciseSet)
            assertEquals(2, ex1Set1.progress.currentExerciseTotalSets)
            assertEquals(1, ex1Set1.progress.remainingExerciseSets)
            assertEquals(3, ex1Set1.progress.remainingSetsInWorkout)

            // 2. Complete Ex 1 Set 1 -> Rest before Ex 1 Set 2
            session.complete()
            val rest1 = session.state.value as TrainingSessionState.RestTimeState
            assertEquals(ex1.id, rest1.nextExercise.exerciseId)
            assertEquals(30.seconds, rest1.totalRestTime)
            val nextProgress1 = rest1.nextSetQueueProgress
            assertNotNull(nextProgress1)
            assertEquals(1, nextProgress1!!.exerciseIndex)
            assertEquals(2, nextProgress1.currentExerciseSet)
            assertEquals(2, nextProgress1.currentExerciseTotalSets)
            assertEquals(0, nextProgress1.remainingExerciseSets)
            assertEquals(2, nextProgress1.remainingSetsInWorkout)

            // 3. Complete Rest -> Ex 1, Set 2 of 2 (remainingExerciseSets == 0, exerciseIndex == 1)
            session.complete()
            val ex1Set2 = session.state.value as TrainingSessionState.ExerciseState
            assertEquals(1, ex1Set2.progress.exerciseIndex)
            assertEquals(2, ex1Set2.progress.totalExercises)
            assertEquals(1, ex1Set2.progress.remainingExercises)
            assertEquals(2, ex1Set2.progress.currentExerciseSet)
            assertEquals(2, ex1Set2.progress.currentExerciseTotalSets)
            assertEquals(0, ex1Set2.progress.remainingExerciseSets)
            assertEquals(2, ex1Set2.progress.remainingSetsInWorkout)

            // 4. Complete Ex 1 Set 2 -> Rest before Ex 2 Set 1 (uses plan restTime = 45s)
            session.complete()
            val rest2 = session.state.value as TrainingSessionState.RestTimeState
            assertEquals(ex2.id, rest2.nextExercise.exerciseId)
            assertEquals(45.seconds, rest2.totalRestTime)
            val nextProgress2 = rest2.nextSetQueueProgress
            assertNotNull(nextProgress2)
            assertEquals(2, nextProgress2!!.exerciseIndex)
            assertEquals(1, nextProgress2.currentExerciseSet)
            assertEquals(2, nextProgress2.currentExerciseTotalSets)
            assertEquals(1, nextProgress2.remainingExerciseSets)
            assertEquals(1, nextProgress2.remainingSetsInWorkout)

            // 5. Skip Rest -> Ex 2, Set 1 of 2
            session.skip()
            val ex2Set1 = session.state.value as TrainingSessionState.ExerciseState
            assertEquals(2, ex2Set1.progress.exerciseIndex)
            assertEquals(1, ex2Set1.progress.currentExerciseSet)
            assertEquals(1, ex2Set1.progress.remainingExerciseSets)
            assertEquals(1, ex2Set1.progress.remainingSetsInWorkout)

            // 6. Complete Ex 2 Set 1 and Rest -> Ex 2, Set 2 of 2 (final set of workout)
            session.complete()
            session.complete()
            val ex2Set2 = session.state.value as TrainingSessionState.ExerciseState
            assertEquals(2, ex2Set2.progress.exerciseIndex)
            assertEquals(2, ex2Set2.progress.currentExerciseSet)
            assertEquals(0, ex2Set2.progress.remainingExerciseSets)
            assertEquals(0, ex2Set2.progress.remainingExercises)
            assertEquals(0, ex2Set2.progress.remainingSetsInWorkout)
            assertNull(ex2Set2.progress.nextSetQueueProgress)
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
            assertEquals(1, afterAdd.progress.currentExerciseSet)

            // when: remove extra set
            trainingSession.removeExtraSet()
            val afterRemove = trainingSession.state.value as TrainingSessionState.ExerciseState
            assertEquals(2, afterRemove.progress.currentExerciseTotalSets)
            assertEquals(1, afterRemove.progress.remainingExerciseSets)
            assertEquals(1, afterRemove.progress.currentExerciseSet)
        }

    @Test
    fun `addExtraSet on the last set of an exercise queues another set of that exercise next`() =
        runTest(UnconfinedTestDispatcher()) {
            // given: single exercise with 1 set
            val ex = createTrainingExercise(sets = 1, restTime = 30.seconds)
            val plan = createTrainingPlan(listOf(ex))
            val events = mutableListOf<SessionEvent>()
            val session =
                createTrainingSession(
                    trainingPlan = plan,
                    scope = backgroundScope,
                    statisticsRecorder = SessionStatisticsRecorder { events.add(it) }
                )

            session.start() // Set 1 of 1 (remainingExerciseSets = 0)
            val initialProgress =
                (session.state.value as TrainingSessionState.ExerciseState).progress
            assertEquals(0, initialProgress.remainingExerciseSets)
            assertEquals(1, initialProgress.currentExerciseSet)

            // when: addExtraSet on the last set
            session.addExtraSet()

            // then: progress updates to Set 1 of 2 (1 remaining) and ExtraSetAdded event is emitted
            val updatedProgress =
                (session.state.value as TrainingSessionState.ExerciseState).progress
            assertEquals(2, updatedProgress.currentExerciseTotalSets)
            assertEquals(1, updatedProgress.remainingExerciseSets)
            assertEquals(1, updatedProgress.currentExerciseSet)
            val extraSetEvent = events.filterIsInstance<SessionEvent.ExtraSetAdded>().single()
            assertEquals(ex.id, extraSetEvent.exercise.exerciseId)
            assertEquals(2, extraSetEvent.newSetIndex)

            // and completing Set 1 transitions to RestTimeState for Set 2 (instead of SummaryState)
            session.complete()
            assertRestTimeState(
                expectedNextExerciseId = ex.id,
                expectedTotalRestTime = plan.restTime,
                trainingSessionState = session.state.value
            )
        }

    @Test
    fun `removeExtraSet when no remaining sets exist for active exercise is a no-op`() =
        runTest(UnconfinedTestDispatcher()) {
            // given: 2 exercises, first exercise has 1 set (remainingExerciseSets == 0 on start)
            val ex1 = createTrainingExercise(name = "Ex 1", sets = 1, restTime = 30.seconds)
            val ex2 = createTrainingExercise(name = "Ex 2", sets = 2, restTime = 30.seconds)
            val plan = createTrainingPlan(listOf(ex1, ex2))
            val session = createTrainingSession(plan, backgroundScope)
            session.start()

            // when: attempt to removeExtraSet when remainingExerciseSets == 0
            session.removeExtraSet()

            // then: progress and subsequent exercises remain untouched
            val progress = (session.state.value as TrainingSessionState.ExerciseState).progress
            assertEquals(1, progress.currentExerciseTotalSets)
            assertEquals(0, progress.remainingExerciseSets)
            assertEquals(2, progress.remainingSetsInWorkout)
        }

    @Test
    fun `updating exercise reps and weight carries over on complete but not on skip`() =
        runTest(UnconfinedTestDispatcher()) {
            // given: Exercise with 3 sets, restTime = 0s
            val ex = createTrainingExercise(sets = 3, repetitions = 10, restTime = Duration.ZERO)
            val plan = createTrainingPlan(listOf(ex))
            val trainingSession = createTrainingSession(plan, backgroundScope)
            trainingSession.start()

            // when: modify reps and weight in Set 1, then complete Set 1
            trainingSession.handle(SessionCommand.Exercise.UpdateReps(12))
            trainingSession.handle(SessionCommand.Exercise.UpdateWeight(82.5))
            trainingSession.complete() // Set 1 -> Set 2

            // then: Set 2 inherits updated reps & weight while preserving plannedConfig
            val set2State = trainingSession.state.value as TrainingSessionState.ExerciseState
            assertEquals(12, set2State.sessionExercise.currentConfig.reps)
            assertEquals(82.5, set2State.sessionExercise.currentConfig.weightInKg!!, 0.01)
            assertEquals(10, set2State.sessionExercise.snapshot.plannedConfig.reps)

            // when: modify reps and weight in Set 2, then SKIP Set 2
            trainingSession.handle(SessionCommand.Exercise.UpdateReps(20))
            trainingSession.handle(SessionCommand.Exercise.UpdateWeight(100.0))
            trainingSession.skip() // Set 2 skipped -> Set 3

            // then: Set 3 keeps Set 1's completed config (12 reps, 82.5kg)
            val set3State = trainingSession.state.value as TrainingSessionState.ExerciseState
            assertEquals(12, set3State.sessionExercise.currentConfig.reps)
            assertEquals(82.5, set3State.sessionExercise.currentConfig.weightInKg!!, 0.01)
        }

    @Test
    fun `isometric exercise initializes Ready timer, overflows, resets, and updates hold time`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val timeProvider = FixedTimeProvider()
            val timerFactory =
                TimerFactory(
                    timeProvider = timeProvider,
                    tickDelay = 1.seconds,
                    timerDispatcher = dispatcher
                )
            val isometricExercise =
                TrainingExercise(
                    id = TrainingExerciseId.create(),
                    repetitions = 1,
                    sets = 1,
                    restTime = 30.seconds,
                    time = 2.seconds,
                    exercise = createDummyExercise("Plank")
                )
            val plan = createTrainingPlan(listOf(isometricExercise))
            val session =
                createTrainingSession(
                    trainingPlan = plan,
                    scope = backgroundScope,
                    timeProvider = timeProvider,
                    timerFactory = timerFactory
                )

            // when: start session
            session.start()
            val startState = session.state.value as TrainingSessionState.ExerciseState
            assertEquals(TimerState.Ready(2.seconds), startState.timerState)

            // when: StartTimer command is handled and time overflows past 2s
            session.handle(SessionCommand.Exercise.StartTimer)
            assertEquals(
                TimerState.Running(2.seconds),
                (session.state.value as TrainingSessionState.ExerciseState).timerState
            )
            timeProvider.advanceBy(3.seconds)
            testScheduler.advanceTimeBy(1000L)
            testScheduler.runCurrent()

            // then: timer enters OverflowRunning and does NOT auto-complete the ExercisePhase
            assertEquals(
                TimerState.OverflowRunning((-1).seconds),
                (session.state.value as TrainingSessionState.ExerciseState).timerState
            )

            // when: ResetTimer command is handled
            session.handle(SessionCommand.Exercise.ResetTimer)
            assertEquals(
                TimerState.Ready(2.seconds),
                (session.state.value as TrainingSessionState.ExerciseState).timerState
            )

            // when: update hold duration to 40s and complete
            session.handle(SessionCommand.Exercise.UpdateHoldDuration(40.seconds))
            assertEquals(
                TimerState.Ready(40.seconds),
                (session.state.value as TrainingSessionState.ExerciseState).timerState
            )
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
    fun `commands for other phases are safely ignored by active phase`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val session = createTrainingSession(TRAINING_PLAN, backgroundScope)
            session.start()
            val beforeExerciseState = session.state.value as TrainingSessionState.ExerciseState

            // when: send RestTime and Summary commands during ExercisePhase
            session.handle(SessionCommand.RestTime.UpdateFeedback(SetEffort.MAXIMAL))
            session.handle(SessionCommand.Summary.UpdateOverallRpe(SessionRpe.MAXIMAL))
            session.handle(SessionCommand.Summary.UpdateNotes("Ignored note"))

            // then: ExerciseState is unchanged
            assertEquals(beforeExerciseState, session.state.value)

            // when: transition to RestPhase and send Exercise and Summary commands
            session.complete()
            val beforeRestState = session.state.value as TrainingSessionState.RestTimeState
            session.handle(SessionCommand.Exercise.UpdateReps(99))
            session.handle(SessionCommand.Summary.UpdateNotes("Ignored in rest"))

            // then: RestTimeState is unchanged
            assertEquals(beforeRestState, session.state.value)
        }

    @Test
    fun `endEarly in ExercisePhase records set as skipped, finishes session, and goes to Idle`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val timeProvider = FixedTimeProvider()
            val recorder = BasicStatisticsRecorder(TRAINING_PLAN.id, timeProvider)
            val events = mutableListOf<SessionEvent>()
            val compositeRecorder =
                SessionStatisticsRecorder { event ->
                    events.add(event)
                    recorder.onEvent(event)
                }
            val trainingSession =
                createTrainingSession(
                    trainingPlan = TRAINING_PLAN,
                    scope = backgroundScope,
                    statisticsRecorder = compositeRecorder,
                    timeProvider = timeProvider
                )
            trainingSession.start()
            trainingSession.complete() // Set 1 completed -> Rest
            trainingSession.complete() // Rest completed -> Set 2 started

            // when: end early during Set 2 (ExercisePhase)
            trainingSession.endEarly()

            // then: transitions back to IdleState, emits SessionFinished, and records Set 2 skipped
            assertEquals(TrainingSessionState.IdleState, trainingSession.state.value)
            assertTrue(events.last() is SessionEvent.SessionFinished)
            val attempts =
                recorder.getStatistics().exercisesStatistics
                    .first()
                    .attemptsStatistics
            assertEquals(2, attempts.size)
            assertTrue(attempts[0].completed)
            assertFalse(attempts[1].completed)
        }

    @Test
    fun `endEarly in RestPhase records rest as completed with effort and returns to IdleState`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val timeProvider = FixedTimeProvider()
            val recorder = BasicStatisticsRecorder(TRAINING_PLAN.id, timeProvider)
            val events = mutableListOf<SessionEvent>()
            val compositeRecorder =
                SessionStatisticsRecorder { event ->
                    events.add(event)
                    recorder.onEvent(event)
                }
            val trainingSession =
                createTrainingSession(
                    trainingPlan = TRAINING_PLAN,
                    scope = backgroundScope,
                    statisticsRecorder = compositeRecorder,
                    timeProvider = timeProvider
                )
            trainingSession.start()
            trainingSession.complete() // Set 1 completed -> RestPhase

            // when: select feedback during RestPhase and end early
            timeProvider.advanceBy(15.seconds)
            trainingSession.handle(SessionCommand.RestTime.UpdateFeedback(SetEffort.HARD))
            trainingSession.endEarly()

            // then: transitions back to IdleState, emits SessionFinished, and Set 1 retains effort
            assertEquals(TrainingSessionState.IdleState, trainingSession.state.value)
            assertTrue(events.last() is SessionEvent.SessionFinished)
            val attempt =
                recorder.getStatistics().exercisesStatistics
                    .first()
                    .attemptsStatistics
                    .first()
            assertTrue(attempt.completed)
            assertEquals(SetEffort.HARD, attempt.effort)
            assertEquals(15.seconds, attempt.restTime)
        }
}
