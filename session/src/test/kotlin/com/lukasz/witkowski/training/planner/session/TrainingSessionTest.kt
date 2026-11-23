package com.lukasz.witkowski.training.planner.session

import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionFactory
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

internal open class TrainingSessionTest {
    private val trainingSetsStrategy: TrainingSetsPolicy = StraightSetsPolicy()

    protected fun createTrainingSession(
        trainingPlan: TrainingPlan,
        scope: CoroutineScope,
        policy: TrainingSetsPolicy = trainingSetsStrategy,
        statisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder(),
        timeProvider: TimeProvider = FixedTimeProvider(),
        timerFactory: TimerFactory = TimerFactory(timeProvider = timeProvider)
    ): TrainingSession = TrainingSessionFactory.create(
        trainingPlan = trainingPlan,
        policy = policy,
        statisticsRecorder = statisticsRecorder,
        timeProvider = timeProvider,
        timerFactory = timerFactory,
        scope = scope
    )

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
        expectedNextExerciseId: TrainingExerciseId,
        expectedTotalRestTime: Duration,
        trainingSessionState: TrainingSessionState
    ) {
        assertTrue(
            "Expected RestTimeState but was $trainingSessionState",
            trainingSessionState is TrainingSessionState.RestTimeState
        )
        val restState = trainingSessionState as TrainingSessionState.RestTimeState
        assertEquals(expectedTotalRestTime, restState.totalRestTime)
        assertEquals(expectedNextExerciseId, restState.nextExercise.exerciseId)
    }

    protected fun assertExerciseState(
        expectedExerciseId: TrainingExerciseId,
        trainingSessionState: TrainingSessionState
    ) {
        assertTrue(
            "Expected ExerciseState but was $trainingSessionState",
            trainingSessionState is TrainingSessionState.ExerciseState
        )
        val exerciseState = trainingSessionState as TrainingSessionState.ExerciseState
        assertEquals(expectedExerciseId, exerciseState.sessionExercise.exerciseId)
    }
}
