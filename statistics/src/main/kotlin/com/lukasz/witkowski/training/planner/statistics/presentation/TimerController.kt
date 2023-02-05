package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import kotlinx.coroutines.flow.StateFlow

interface TimerController {
    /**
     * Emits time in the defined intervals.
     */
    val timer: StateFlow<Time>

    /**
     * Emits _true_ if the timer has finished.
     * When the timer was stopped by the [stopTimer] method call, it remains _false_.
     */
    val hasFinished: StateFlow<Boolean>

    /**
     * Emits _true_ if the timer is running, _false_ if timer is stopped or paused.
     */
    val isRunning: StateFlow<Boolean>

    /**
     * Set the [startTime] for the timer. The value after initialization is 0.
     */
    fun setTimer(startTime: Time)

    fun startTimer()

    fun pauseTimer()

    /**
     * Resume timer after it was paused. If the timer was not started, it starts it from the set time.
     */
    fun resumeTimer()

    /**
     * Stop the timer and reset to the initial time set by [setTimer] method. (should it reset?? I have a method for that)
     */
    fun stopTimer()

    /**
     * Reset the timer to the initial time. If the timer is running, it stops.
     */
    fun resetTimer()
}
