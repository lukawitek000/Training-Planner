package com.lukasz.witkowski.training.planner.statistics.presentation

import kotlinx.coroutines.flow.StateFlow

interface TimerController {

    val timer: StateFlow<Long>
    fun setTime(time: Long)
    fun start()
    fun pause()
    fun resume()
}