package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import java.util.Date

/**
 * Domain events emitted during an active training session.
 * Used by [SessionStatisticsRecorder] implementations to capture metrics, sensor telemetry,
 * heart rate, velocity, or user performance.
 */
sealed class SessionEvent {
    data class SessionStarted(
        val trainingPlan: TrainingPlan,
        val startTime: Time,
        val date: Date = Date()
    ) : SessionEvent()

    data class ExerciseStarted(val exercise: TrainingExercise, val set: Int, val startTime: Time) :
        SessionEvent()

    data class ExerciseCompleted(val attempt: ExerciseAttemptStatistics, val stopTime: Time) :
        SessionEvent()

    data class ExerciseSkipped(val exercise: TrainingExercise, val set: Int, val time: Time) :
        SessionEvent()

    data class RestStarted(val restTime: Time, val startTime: Time) : SessionEvent()

    data class RestCompleted(val time: Time) : SessionEvent()

    data class RestSkipped(val time: Time) : SessionEvent()

    data class ExtraSetAdded(val exercise: TrainingExercise, val newSetIndex: Int) : SessionEvent()

    data class SessionFinished(val totalTime: Time, val endTime: Time) : SessionEvent()
}
