package com.lukasz.witkowski.training.planner.statistics.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

// TODO WHere to put timer implementation in which layer
class DefaultTimerController: TimerController {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val _timer = MutableStateFlow(0L)
    override val timer: StateFlow<Long>
        get() = _timer

    private var isRunning = false
    private var isPaused = false

    override fun setTime(time: Long) {
        _timer.value = time
    }

    override fun startTimer() {
        coroutineScope.launch {
            isRunning = true
            while (isRunning && _timer.value >= DELAY_IN_MILLIS) {
                delay(DELAY_IN_MILLIS)
                _timer.value -= DELAY_IN_MILLIS
            }
        }
    }

    override fun stopTimer() {
//        coroutineScope.cancel()
        isRunning = false
    }

    override fun pauseTimer() {
//        TODO("Not yet implemented")
    }

    override fun resumeTimer() {
//        TODO("Not yet implemented")
    }

    private companion object {
        const val DELAY_IN_MILLIS = 10L
    }
}