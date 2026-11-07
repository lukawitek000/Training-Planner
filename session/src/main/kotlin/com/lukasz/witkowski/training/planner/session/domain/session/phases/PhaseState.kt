package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import kotlin.time.Duration

internal sealed interface PhaseState {
    fun toTrainingSessionState(progress: ExerciseQueueProgress): TrainingSessionState

    data object Idle : PhaseState {
        override fun toTrainingSessionState(progress: ExerciseQueueProgress): TrainingSessionState =
            TrainingSessionState.IdleState
    }

    data class Exercise(val exercise: SessionExercise, val timerState: TimerState) : PhaseState {
        override fun toTrainingSessionState(progress: ExerciseQueueProgress): TrainingSessionState =
            TrainingSessionState.ExerciseState(
                sessionExercise = exercise,
                timerState = timerState,
                progress = progress
            )
    }

    data class RestTime(
        val nextExercise: SessionExercise,
        val timerState: TimerState,
        val totalRestTime: Duration,
        val selectedFeedback: Int? = null
    ) : PhaseState {
        override fun toTrainingSessionState(progress: ExerciseQueueProgress): TrainingSessionState =
            TrainingSessionState.RestTimeState(
                nextExercise = nextExercise,
                timerState = timerState,
                totalRestTime = totalRestTime,
                progress = progress,
                selectedFeedback = selectedFeedback
            )
    }

    data class Summary(
        val statistics: TrainingStatistics,
        val selectedOverallRpe: Int? = null,
        val notes: String = ""
    ) : PhaseState {
        override fun toTrainingSessionState(progress: ExerciseQueueProgress): TrainingSessionState =
            TrainingSessionState.SummaryState(
                statistics = statistics,
                selectedOverallRpe = selectedOverallRpe,
                notes = notes
            )
    }
}
