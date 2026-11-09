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
import com.lukasz.witkowski.training.planner.shared.time.Time
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `training session dispatches lifecycle events to recorder`() =
        runTest(UnconfinedTestDispatcher()) {
            val events = mutableListOf<SessionEvent>()
            val recorder = SessionStatisticsRecorder { events.add(it) }
            val timeProvider = FixedTimeProvider()

            val plan =
                createTrainingPlan(
                    listOf(
                        createTrainingExercise(sets = 1, restTime = 60.seconds),
                        createTrainingExercise(sets = 1, restTime = 0.seconds)
                    )
                )

            val session =
                TrainingSessionFactory.create(
                    trainingPlan = plan,
                    policy = StraightSetsPolicy(),
                    statisticsRecorder = recorder,
                    timeProvider = timeProvider,
                    scope = backgroundScope
                )

            // 1. Start session
            session.start()
            assertTrue(events.any { it is SessionEvent.SessionStarted })
            assertTrue(events.any { it is SessionEvent.ExerciseStarted })

            // 2. Complete first exercise (triggers rest interval)
            timeProvider.advanceBy(Time(30000L))
            session.handle(SessionCommand.Exercise.UpdateReps(10))
            session.handle(SessionCommand.Exercise.UpdateWeight(50.0))
            session.complete()
            assertTrue(events.any { it is SessionEvent.ExerciseCompleted })
            assertTrue(events.any { it is SessionEvent.RestStarted })

            // 3. Complete rest (transitions to next exercise)
            timeProvider.advanceBy(Time(60000L))
            session.complete()
            assertTrue(events.any { it is SessionEvent.RestCompleted })

            // 4. Complete second exercise (finishes session)
            timeProvider.advanceBy(Time(30000L))
            session.handle(SessionCommand.Exercise.UpdateReps(8))
            session.handle(SessionCommand.Exercise.UpdateWeight(60.0))
            session.complete()
            assertTrue(events.any { it is SessionEvent.SessionFinished })
        }

    @Test
    fun `basic statistics recorder creates statistics from session events`() =
        runTest(UnconfinedTestDispatcher()) {
            val fixedTimeProvider = FixedTimeProvider(time = Time(0L), date = Date(1000000L))
            val recorder = BasicStatisticsRecorder(timeProvider = fixedTimeProvider)

            val plan =
                createTrainingPlan(
                    listOf(createTrainingExercise(sets = 1))
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
            fixedTimeProvider.advanceBy(Time(45000L))
            session.handle(SessionCommand.Exercise.UpdateReps(12))
            session.handle(SessionCommand.Exercise.UpdateWeight(40.0))
            session.complete()

            val stats = recorder.getStatistics()
            assertEquals(Time(45000L), stats.totalTime)
            assertEquals(1, stats.exercisesStatistics.size)
            assertEquals(1, stats.exercisesStatistics[0].attemptsStatistics.size)
            assertEquals(12, stats.exercisesStatistics[0].attemptsStatistics[0].actualReps)
            assertEquals(
                40.0,
                stats.exercisesStatistics[0].attemptsStatistics[0].actualWeightInKg!!,
                0.01
            )
        }

    @Test
    fun `basic recorder records rest duration, preserves id, and saves summary rpe and notes`() =
        runTest(UnconfinedTestDispatcher()) {
            val fixedTimeProvider = FixedTimeProvider(time = Time(0L), date = Date(1000000L))
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
            fixedTimeProvider.advanceBy(Time(30000L))
            session.complete() // Set 1 -> Rest

            fixedTimeProvider.advanceBy(Time(25000L))
            session.handle(SessionCommand.RestTime.UpdateFeedback(SetEffort.HARD))
            session.skip() // Skip rest after 25s -> Set 2

            fixedTimeProvider.advanceBy(Time(35000L))
            session.complete() // Set 2 -> Summary

            val statsBeforeSummaryComplete = recorder.getStatistics()

            session.handle(SessionCommand.Summary.UpdateOverallRpe(SessionRpe.NEAR_LIMIT))
            session.handle(SessionCommand.Summary.UpdateNotes("Solid session"))
            session.complete() // Complete summary

            val finalStats = recorder.getStatistics()

            assertEquals(statsBeforeSummaryComplete.id, finalStats.id)
            assertEquals(SessionRpe.NEAR_LIMIT, finalStats.rpe)
            assertEquals("Solid session", finalStats.note)

            val firstAttempt = finalStats.exercisesStatistics.first().attemptsStatistics.first()
            assertEquals(25.seconds, firstAttempt.restTime)
            assertEquals(SetEffort.HARD, firstAttempt.effort)
        }
}
