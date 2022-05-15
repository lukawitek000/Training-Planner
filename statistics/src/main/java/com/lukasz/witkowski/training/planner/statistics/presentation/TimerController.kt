package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import kotlinx.coroutines.flow.StateFlow

interface TimerController {
    val timer: StateFlow<Time>
    val hasFinished: StateFlow<Boolean>
    val isRunning: StateFlow<Boolean>
    fun setTimer(startTime: Time)
    fun startTimer()
    fun pauseTimer()
    fun resumeTimer()
    fun stopTimer()
    fun resetTimer()
}