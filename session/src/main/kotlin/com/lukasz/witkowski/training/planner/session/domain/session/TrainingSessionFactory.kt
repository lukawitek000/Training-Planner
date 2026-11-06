package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.session.phases.NextPhaseProvider
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueue
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineScope

internal object TrainingSessionFactory {
    fun create(
        trainingPlan: TrainingPlan,
        policy: TrainingSetsPolicy,
        statisticsRecorder: SessionStatisticsRecorder,
        scope: CoroutineScope
    ): TrainingSession {
        require(trainingPlan.exercises.isNotEmpty()) {
            "Cannot start training session without exercises"
        }
        val exerciseQueue = ExerciseQueue(
            loadedExercises = policy.loadExercises(trainingPlan),
        )
        return TrainingSession(
            exerciseQueue = exerciseQueue,
            nextPhaseProvider = NextPhaseProvider(exerciseQueue, statisticsRecorder),
            scope = scope
        )
    }
}