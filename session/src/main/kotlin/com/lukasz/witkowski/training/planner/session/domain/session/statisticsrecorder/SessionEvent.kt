package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.time.Duration

/**
 * Domain events emitted during an active training session.
 * Used by [SessionStatisticsRecorder] implementations to capture metrics, sensor telemetry,
 * heart rate, velocity, or user performance.
 */
sealed interface SessionEvent {
    data class SessionStarted(val trainingPlanId: TrainingPlanId) : SessionEvent

    data class ExerciseStarted(val exercise: SessionExercise) : SessionEvent

    data class ExerciseCompleted(val exercise: SessionExercise) : SessionEvent

    data class ExerciseSkipped(val exercise: SessionExercise) : SessionEvent

    data class RestStarted(val restTime: Duration) : SessionEvent

    data class RestCompleted(val rpeFeedback: Int? = null) : SessionEvent

    data class RestSkipped(val rpeFeedback: Int? = null) : SessionEvent

    data class ExtraSetAdded(val exercise: SessionExercise, val newSetIndex: Int) : SessionEvent

    data object SessionFinished : SessionEvent

    data class SummaryCompleted(val overallRpe: Int? = null, val notes: String? = null) :
        SessionEvent
}
