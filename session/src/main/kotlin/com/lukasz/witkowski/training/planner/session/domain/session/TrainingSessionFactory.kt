package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.session.phases.NextPhaseProvider
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueue
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineScope

internal object TrainingSessionFactory {
    fun create(
        trainingPlan: TrainingPlan,
        policy: TrainingSetsPolicy = StraightSetsPolicy(),
        statisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder(),
        timeProvider: TimeProvider = SystemTimeProvider(),
        timerFactory: TimerFactory = TimerFactory(timeProvider = timeProvider),
        scope: CoroutineScope
    ): TrainingSession {
        require(trainingPlan.exercises.isNotEmpty()) {
            "Cannot start training session without exercises"
        }
        val exerciseQueue =
            ExerciseQueue(
                loadedExercises = policy.loadExercises(trainingPlan)
            )
        val basicRecorder =
            BasicStatisticsRecorder(
                trainingPlanId = trainingPlan.id,
                timeProvider = timeProvider
            )
        val compositeRecorder =
            statisticsRecorder as? BasicStatisticsRecorder
                ?: CompositeStatisticsRecorder(basicRecorder, statisticsRecorder)
        val statisticsProvider =
            statisticsRecorder as? BasicStatisticsRecorder ?: basicRecorder
        return TrainingSession(
            trainingPlanId = trainingPlan.id,
            exerciseQueue = exerciseQueue,
            nextPhaseProvider =
            NextPhaseProvider(
                exerciseQueue = exerciseQueue,
                statisticsProvider = statisticsProvider,
                timerFactory = timerFactory
            ),
            statisticsRecorder = compositeRecorder,
            scope = scope
        )
    }
}
