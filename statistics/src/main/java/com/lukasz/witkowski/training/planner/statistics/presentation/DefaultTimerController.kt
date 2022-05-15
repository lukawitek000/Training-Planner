package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

class DefaultTimerController: TimerController {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null
    private val _timer = MutableStateFlow(Time.NONE)
    override val timer: StateFlow<Time>
        get() = _timer

    override val hasFinished: StateFlow<Boolean>
        get() = _timer.map { it.timeInMillis < DELAY_IN_MILLIS }.stateIn(coroutineScope, SharingStarted.Lazily, false)


    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean>
        get() = _isRunning
    private var initialTime = Time.NONE

    override fun startTimer() {
        timerJob = coroutineScope.launch {
            _isRunning.value = true
            while (isRunning.value && _timer.value.timeInMillis >= DELAY_IN_MILLIS) {
                delay(DELAY_IN_MILLIS)
                _timer.value = Time(timer.value.timeInMillis - DELAY_IN_MILLIS)
            }
        }
    }

    override fun stopTimer() {
        pauseTimer()
        setTimer(initialTime)
    }

    override fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    override fun resumeTimer() {
        startTimer()
    }

    override fun resetTimer() {
        stopTimer()
        _timer.value = initialTime
    }

    override fun setTimer(startTime: Time) {
        initialTime = startTime
        _timer.value = initialTime
    }

    private companion object {
        const val DELAY_IN_MILLIS = 10L
    }
}