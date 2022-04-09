package com.lukasz.witkowski.training.planner.statistics.presentation

import kotlinx.coroutines.flow.StateFlow

interface TimerController {
    // TODO wrap Long in some Duration??
    // For presentation I use TimeFormatter so separate classes or Duration should have methods that returns formatted Strings??
    val timer: StateFlow<Long>
    val hasFinished: StateFlow<Boolean>
    val isRunning: StateFlow<Boolean>
    fun setTimer(startTime: Long)
    fun startTimer()
    fun pauseTimer()
    fun resumeTimer()
    fun stopTimer()
    fun resetTimer()
}