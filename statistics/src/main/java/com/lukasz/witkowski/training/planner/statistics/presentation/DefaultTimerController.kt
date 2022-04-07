package com.lukasz.witkowski.training.planner.statistics.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DefaultTimerController: TimerController {
    private val _timer = MutableStateFlow(0L)
    override val timer: StateFlow<Long>
        get() = _timer

    override fun setTime(time: Long) {
        _timer.value = time
    }

    override fun start() {
//        TODO("Not yet implemented")
    }

    override fun pause() {
//        TODO("Not yet implemented")
    }

    override fun resume() {
//        TODO("Not yet implemented")
    }
}