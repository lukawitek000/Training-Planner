package com.lukasz.witkowski.training.planner.session.domain.timer

import kotlin.time.Duration

sealed interface TimerState {
    data class Running(
        val remaining: Duration
    ) : TimerState

    data class OverflowRunning(
        val overflow: Duration
    ) : TimerState

    data object Finished : TimerState

    data class Ready(
        val duration: Duration
    ) : TimerState

    data object NotSet : TimerState
}