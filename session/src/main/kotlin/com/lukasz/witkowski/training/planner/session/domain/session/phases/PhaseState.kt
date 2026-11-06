package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import kotlin.time.Duration

sealed interface PhaseState {
    data object Idle: PhaseState

    data class Exercise(
        val exercise: SessionExercise,
        val timerState: TimerState
    ) : PhaseState

    data class RestTime(
        val nextExercise: SessionExercise,
        val timerState: TimerState,
        val totalRestTime: Duration,
    ) : PhaseState
}