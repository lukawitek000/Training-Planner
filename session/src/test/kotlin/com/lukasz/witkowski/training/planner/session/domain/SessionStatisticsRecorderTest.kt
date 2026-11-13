package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.createTrainingExercise
import com.lukasz.witkowski.training.planner.session.createTrainingPlan
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionFactory
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import java.util.Date
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionStatisticsRecorderTest {

    @Test
    fun `composite recorder forwards events to all child recorders`() {
        val receivedEvents1 = mutableListOf<SessionEvent>()
        val receivedEvents2 = mutableListOf<SessionEvent>()

        val recorder1 = SessionStatisticsRecorder { receivedEvents1.add(it) }
        val recorder2 = SessionStatisticsRecorder { receivedEvents2.add(it) }

        val composite = CompositeStatisticsRecorder(recorder1, recorder2)
        val event = SessionEvent.RestCompleted(feedback = SetEffort.HARD)

        composite.onEvent(event)

        assertEquals(listOf(event), receivedEvents1)
        assertEquals(listOf(event), receivedEvents2)
    }

    @Test
    fun `training session dispatches all lifecycle events in order to recorder`() =
        runTest(UnconfinedTestDispatcher()) {
            val events = mutableListOf<SessionEvent>()
            val recorder = SessionStatisticsRecorder { events.add(it) }
            val timeProvider = FixedTimeProvider()

            val ex1 = createTrainingExercise(name = "Ex 1", sets = 1, restTime = 60.seconds)
            val ex2 = createTrainingExercise(name = "Ex 2", sets = 1, restTime = 0.seconds)
            val plan = createTrainingPlan(listOf(ex1, ex2), restTime = 45.seconds)

            val session =
                TrainingSessionFactory.create(
                    trainingPlan = plan,
                    policy = StraightSetsPolicy(),
                    statisticsRecorder = recorder,
                    timeProvider = timeProvider,
                    scope = backgroundScope
                )

            // 1. Start session -> SessionStarted, ExerciseStarted(Ex 1 Set 1)
            session.start()
            assertEquals(SessionEvent.SessionStarted(plan.id), events[0])
            assertTrue(events[1] is SessionEvent.ExerciseStarted)

            // 2. Add extra set -> ExtraSetAdded(Ex 1, 2)
            session.addExtraSet()
            val extraSetEvent = events[2] as SessionEvent.ExtraSetAdded
            assertEquals(ex1.id, extraSetEvent.exercise.exerciseId)
            assertEquals(2, extraSetEvent.newSetIndex)

            // 3. Complete Ex 1 Set 1 -> ExerciseCompleted, RestStarted
            timeProvider.advanceBy(30.seconds)
            session.handle(SessionCommand.Exercise.UpdateReps(10))
            session.handle(SessionCommand.Exercise.UpdateWeight(50.0))
            session.complete()
            assertTrue(events[3] is SessionEvent.ExerciseCompleted)
            assertEquals(SessionEvent.RestStarted(45.seconds), events[4])

            // 4. Complete rest with feedback -> RestCompleted(MODERATE), ExerciseStarted(Ex 1 Set 2)
            timeProvider.advanceBy(45.seconds)
            session.handle(SessionCommand.RestTime.UpdateFeedback(SetEffort.MODERATE))
            session.complete()
            assertEquals(SessionEvent.RestCompleted(SetEffort.MODERATE), events[5])
            assertTrue(events[6] is SessionEvent.ExerciseStarted)

            // 5. Skip Ex 1 Set 2 -> ExerciseSkipped, RestStarted
            timeProvider.advanceBy(10.seconds)
            session.skip()
            assertTrue(events[7] is SessionEvent.ExerciseSkipped)
            assertEquals(SessionEvent.RestStarted(45.seconds), events[8])

            // 6. Skip rest with feedback -> RestSkipped(HARD), ExerciseStarted(Ex 2 Set 1)
            timeProvider.advanceBy(20.seconds)
            session.handle(SessionCommand.RestTime.UpdateFeedback(SetEffort.HARD))
            session.skip()
            assertEquals(SessionEvent.RestSkipped(SetEffort.HARD), events[9])
            assertTrue(events[10] is SessionEvent.ExerciseStarted)

            // 7. Complete Ex 2 Set 1 (final set) -> ExerciseCompleted, SessionFinished
            timeProvider.advanceBy(30.seconds)
            session.complete()
            assertTrue(events[11] is SessionEvent.ExerciseCompleted)
            assertEquals(SessionEvent.SessionFinished, events[12])

            // 8. Update summary RPE and notes and complete summary -> SummaryCompleted
            session.handle(SessionCommand.Summary.UpdateOverallRpe(SessionRpe.VERY_HARD))
            session.handle(SessionCommand.Summary.UpdateNotes("Great workout"))
            session.complete()
            assertEquals(
                SessionEvent.SummaryCompleted(
                    overallRpe = SessionRpe.VERY_HARD,
                    notes = "Great workout"
                ),
                events[13]
            )
        }

    @Test
    fun `basic statistics recorder creates statistics from session events`() =
        runTest(UnconfinedTestDispatcher()) {
            val fixedTimeProvider = FixedTimeProvider(time = Duration.ZERO, date = Date(1000000L))
            val recorder = BasicStatisticsRecorder(timeProvider = fixedTimeProvider)

            val plan =
                createTrainingPlan(
                    listOf(createTrainingExercise(name = "90° Hold", sets = 1))
                )

            val session =
                TrainingSessionFactory.create(
                    trainingPlan = plan,
                    policy = StraightSetsPolicy(),
                    statisticsRecorder = recorder,
                    timeProvider = fixedTimeProvider,
                    scope = backgroundScope
                )

            session.start()
            fixedTimeProvider.advanceBy(45.seconds)
            session.handle(SessionCommand.Exercise.UpdateReps(12))
            session.handle(SessionCommand.Exercise.UpdateWeight(40.0))
            session.complete()

            val stats = recorder.getStatistics()
            assertEquals(45.seconds, stats.totalTime)
            assertEquals(1, stats.exercisesStatistics.size)
            assertEquals("90° Hold", stats.exercisesStatistics[0].exerciseName)
            assertEquals(1, stats.exercisesStatistics[0].attemptsStatistics.size)
            assertEquals(12, stats.exercisesStatistics[0].attemptsStatistics[0].actualReps)
            assertEquals(
                40.0,
                stats.exercisesStatistics[0].attemptsStatistics[0].actualWeightInKg!!,
                0.01
            )
        }

    @Test
    fun `basic recorder records rest, skipped sets, preserves id, and saves summary rpe`() =
        runTest(UnconfinedTestDispatcher()) {
            val fixedTimeProvider = FixedTimeProvider(time = Duration.ZERO, date = Date(1000000L))
            val recorder = BasicStatisticsRecorder(timeProvider = fixedTimeProvider)

            val plan =
                createTrainingPlan(
                    listOf(createTrainingExercise(sets = 2, restTime = 60.seconds))
                )

            val session =
                TrainingSessionFactory.create(
                    trainingPlan = plan,
                    policy = StraightSetsPolicy(),
                    statisticsRecorder = recorder,
                    timeProvider = fixedTimeProvider,
                    scope = backgroundScope
                )

            session.start()
            fixedTimeProvider.advanceBy(30.seconds)
            session.complete() // Set 1 completed -> Rest

            fixedTimeProvider.advanceBy(25.seconds)
            session.handle(SessionCommand.RestTime.UpdateFeedback(SetEffort.HARD))
            session.skip() // Skip rest after 25s -> Set 2

            fixedTimeProvider.advanceBy(35.seconds)
            session.skip() // Set 2 skipped -> Summary

            val statsBeforeSummaryComplete = recorder.getStatistics()

            session.handle(SessionCommand.Summary.UpdateOverallRpe(SessionRpe.NEAR_LIMIT))
            session.handle(SessionCommand.Summary.UpdateNotes("Solid session"))
            session.complete() // Complete summary

            val finalStats = recorder.getStatistics()

            assertEquals(statsBeforeSummaryComplete.id, finalStats.id)
            assertEquals(
                statsBeforeSummaryComplete.exercisesStatistics.first().id,
                finalStats.exercisesStatistics.first().id
            )
            assertEquals(SessionRpe.NEAR_LIMIT, finalStats.rpe)
            assertEquals("Solid session", finalStats.note)

            val attempts = finalStats.exercisesStatistics.first().attemptsStatistics
            assertEquals(2, attempts.size)
            assertTrue(attempts[0].completed)
            assertEquals(25.seconds, attempts[0].restTime)
            assertEquals(SetEffort.HARD, attempts[0].effort)
            assertFalse(attempts[1].completed)
            assertEquals(2, attempts[1].set)
        }

    @Test
    fun `basic recorder normalizes blank summary notes to null`() =
        runTest(UnconfinedTestDispatcher()) {
            val fixedTimeProvider = FixedTimeProvider(time = Duration.ZERO, date = Date(1000000L))
            val recorder = BasicStatisticsRecorder(timeProvider = fixedTimeProvider)
            val plan = createTrainingPlan(listOf(createTrainingExercise(sets = 1)))

            val session =
                TrainingSessionFactory.create(
                    trainingPlan = plan,
                    policy = StraightSetsPolicy(),
                    statisticsRecorder = recorder,
                    timeProvider = fixedTimeProvider,
                    scope = backgroundScope
                )

            session.start()
            session.complete() // Set 1 -> Summary
            session.handle(SessionCommand.Summary.UpdateNotes("   "))
            session.complete() // Complete summary

            assertNull(recorder.getStatistics().note)
        }

    @Test
    fun `basic recorder fails fast if getStatistics is called before SessionStarted`() {
        val fixedTimeProvider = FixedTimeProvider(time = Duration.ZERO, date = Date(1000000L))
        val recorder = BasicStatisticsRecorder(timeProvider = fixedTimeProvider)

        assertFailsWith<IllegalStateException> {
            recorder.getStatistics()
        }
    }

    @Test
    fun `basic recorder ignores non-start events before SessionStarted`() {
        val fixedTimeProvider = FixedTimeProvider(time = Duration.ZERO, date = Date(1000000L))
        val recorder = BasicStatisticsRecorder(timeProvider = fixedTimeProvider)

        recorder.onEvent(SessionEvent.SessionFinished)
        recorder.onEvent(SessionEvent.RestCompleted(feedback = SetEffort.HARD))

        assertFailsWith<IllegalStateException> {
            recorder.getStatistics()
        }
    }
}
