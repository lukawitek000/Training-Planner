package com.lukasz.witkowski.training.planner.statistics.presentation

import kotlinx.coroutines.flow.StateFlow

interface TimerController {
    // TODO wrap Long in some Duration??
    // For presentation I use TimeFormatter so separate classes or Duration should have methods that returns formatted Strings??
    val timer: StateFlow<Long>
    fun setTime(time: Long)
    fun start()
    fun pause()
    fun resume()
}