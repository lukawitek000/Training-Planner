package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import kotlin.time.Duration

sealed interface TrainingSessionState {
    data object IdleState : TrainingSessionState

    data class ExerciseState(
        val sessionExercise: SessionExercise,
        val timerState: TimerState,
        val progress: ExerciseQueueProgress
    ) : TrainingSessionState

    data class RestTimeState(
        val nextExercise: SessionExercise,
        val timerState: TimerState,
        val totalRestTime: Duration,
        val progress: ExerciseQueueProgress,
        val selectedFeedback: Int? = null
    ) : TrainingSessionState

    data class SummaryState(
        val statistics: TrainingStatistics,
        val selectedOverallRpe: Int? = null,
        val notes: String = ""
    ) : TrainingSessionState
}
