package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.session.phases.NextPhaseProvider
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueue
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsProvider
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
        statisticsRecorder: SessionStatisticsRecorder? = null,
        additionalRecorders: List<SessionStatisticsRecorder> = emptyList(),
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
        val primaryProvider = (statisticsRecorder as? SessionStatisticsProvider)
            ?: BasicStatisticsRecorder(timeProvider = timeProvider)
        val allRecorders = buildList {
            add(primaryProvider as SessionStatisticsRecorder)
            if (statisticsRecorder != null && statisticsRecorder !== primaryProvider) {
                add(statisticsRecorder)
            }
            addAll(additionalRecorders)
        }.distinct()
        val compositeRecorder = if (allRecorders.size == 1) {
            allRecorders.first()
        } else {
            CompositeStatisticsRecorder(allRecorders)
        }

        return TrainingSession(
            trainingPlanId = trainingPlan.id,
            exerciseQueue = exerciseQueue,
            nextPhaseProvider =
            NextPhaseProvider(
                exerciseQueue = exerciseQueue,
                statisticsProvider = primaryProvider,
                timerFactory = timerFactory
            ),
            statisticsRecorder = compositeRecorder,
            scope = scope
        )
    }
}
