package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration

sealed interface TrainingSessionState {
    object IdleState : TrainingSessionState

    class ExerciseState(
        val sessionExercise: SessionExercise,
        val timerState: TimerState?,
        val progress: ExerciseQueueProgress,
    ) : TrainingSessionState

    class RestTimeState(
        val nextExercise: SessionExercise,
        val timerState: TimerState,
        val totalRestTime: Duration,
        val progress: ExerciseQueueProgress,
    ) : TrainingSessionState

    data class SummaryState(
        val statistics: TrainingStatistics,
        val trainingPlan: TrainingPlan,
        val selectedOverallRpe: Int? = null,
        val notes: String = ""
    ) : TrainingSessionState
}
