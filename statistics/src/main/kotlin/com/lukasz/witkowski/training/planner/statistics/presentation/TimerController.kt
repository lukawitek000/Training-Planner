package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import kotlinx.coroutines.flow.StateFlow

interface TimerController {
    val timer: StateFlow<Time>

    /**
     * Emits _true_ if the timer has finished.
     * When the timer was stopped by the [stopTimer] method call, it remains _false_.
     */
    val hasFinished: StateFlow<Boolean>
    val isRunning: StateFlow<Boolean>
    fun setTimer(startTime: Time)
    fun startTimer()
    fun pauseTimer()
    fun resumeTimer()

    /**
     * Stop the timer and reset to the initial time set by [setTimer] method. (should it reset?? I have a method for that)
     */
    fun stopTimer()

    fun resetTimer()
}
