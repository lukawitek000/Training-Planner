package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.createTrainingExercise
import com.lukasz.witkowski.training.planner.session.createTrainingPlan
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.shared.time.Time
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionStatisticsRecorderTest {

    @Test
    fun `composite recorder forwards events to all child recorders`() {
        val receivedEvents1 = mutableListOf<SessionEvent>()
        val receivedEvents2 = mutableListOf<SessionEvent>()

        val recorder1 = SessionStatisticsRecorder { receivedEvents1.add(it) }
        val recorder2 = SessionStatisticsRecorder { receivedEvents2.add(it) }

        val composite = CompositeStatisticsRecorder(recorder1, recorder2)
        val event = SessionEvent.RestCompleted(Time(1000L))

        composite.onEvent(event)

        assertEquals(listOf(event), receivedEvents1)
        assertEquals(listOf(event), receivedEvents2)
    }

    @Test
    fun `training session dispatches lifecycle events to recorder`() {
        val events = mutableListOf<SessionEvent>()
        val recorder = SessionStatisticsRecorder { events.add(it) }

        val plan = createTrainingPlan(
            listOf(
                createTrainingExercise(sets = 1, restTime = 60.seconds),
                createTrainingExercise(sets = 1, restTime = 0.seconds)
            )
        )

        val session = TrainingSession(
            trainingPlan = plan,
            trainingSetsPolicy = StraightSetsPolicy(),
            statisticsRecorder = recorder
        )

        // 1. Start session
        session.start(Time(0L))
        assertTrue(events.any { it is SessionEvent.SessionStarted })
        assertTrue(events.any { it is SessionEvent.ExerciseStarted })

        // 2. Complete first exercise (triggers rest interval)
        session.completed(Time(30000L), actualReps = 10, actualWeightInKg = 50.0)
        assertTrue(events.any { it is SessionEvent.ExerciseCompleted })
        assertTrue(events.any { it is SessionEvent.RestStarted })

        // 3. Complete rest (transitions to next exercise)
        session.completed(Time(90000L))
        assertTrue(events.any { it is SessionEvent.RestCompleted })

        // 4. Complete second exercise (finishes session)
        session.completed(Time(120000L), actualReps = 8, actualWeightInKg = 60.0)
        assertTrue(events.any { it is SessionEvent.SessionFinished })
    }

    @Test
    fun `basic statistics recorder creates statistics from session events`() {
        val fixedTimeProvider = FixedTimeProvider(time = Time(0L), date = Date(1000000L))
        val recorder = BasicStatisticsRecorder(timeProvider = fixedTimeProvider)

        val plan = createTrainingPlan(
            listOf(createTrainingExercise(sets = 1))
        )

        val session = TrainingSession(
            trainingPlan = plan,
            trainingSetsPolicy = StraightSetsPolicy(),
            statisticsRecorder = recorder
        )

        session.start(Time(0L))
        session.completed(Time(45000L), actualReps = 12, actualWeightInKg = 40.0)

        val stats = recorder.gatherTrainingStatistics(Time(45000L))
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
}
