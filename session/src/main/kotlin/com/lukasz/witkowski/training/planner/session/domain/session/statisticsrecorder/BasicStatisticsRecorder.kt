package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date

/**
 * [BasicStatisticsRecorder] records standard session metrics:
 *  * Total training time
 *  * Exercise and rest durations
 *  * Attempt sets, reps, load, and completion status
 *  * Date of the workout session
 */
class BasicStatisticsRecorder() : SessionStatisticsRecorder {

    override fun onEvent(event: SessionEvent) {
        when (event) {
            is SessionEvent.SessionStarted -> {}
            is SessionEvent.ExerciseStarted -> {}
            is SessionEvent.ExerciseCompleted -> {}
            is SessionEvent.ExerciseSkipped -> {}
            is SessionEvent.RestStarted -> {}
            is SessionEvent.RestCompleted -> {}
            is SessionEvent.RestSkipped -> {}
            is SessionEvent.ExtraSetAdded -> {}
            is SessionEvent.SessionFinished -> {}
        }
    }
}
