package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.statistics.domain.CountdownTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// TODO WHere to put timer implementation in which layer
class DefaultTimerController: TimerController {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val _timer = MutableStateFlow(0L)
    override val timer: StateFlow<Long>
        get() = _timer

    private val timerHelper = CountdownTimer()

    override fun setTime(time: Long) {
        _timer.value = time
    }

    override fun start() {
        coroutineScope.launch {
            timerHelper.start(timer.value, SECOND_IN_MILLIS).collectLatest {
                _timer.value = it
            }
        }
    }

    override fun pause() {
//        TODO("Not yet implemented")
    }

    override fun resume() {
//        TODO("Not yet implemented")
    }

    private companion object {
        const val SECOND_IN_MILLIS = 1000L
    }
}