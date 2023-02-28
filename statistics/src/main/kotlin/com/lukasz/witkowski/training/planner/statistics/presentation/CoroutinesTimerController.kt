package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.shared.time.Time
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoroutinesTimerController(
    private val tickDelayInMillis: Long = DELAY_IN_MILLIS,
    timerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : TimerController {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(timerDispatcher + CoroutineName("DefaultTimerController"))
    private var timerJob: Job? = null

    private val _timer = MutableStateFlow(Time.ZERO)
    override val timer: StateFlow<Time>
        get() = _timer

    private val _hasFinished = MutableStateFlow(false)
    override val hasFinished: StateFlow<Boolean>
        get() = _hasFinished

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean>
        get() = _isRunning
    private var initialTime = Time.ZERO

    override fun startTimer() {
        startTimer(initialTime)
    }

    private fun startTimer(initTime: Time) {
        timerJob = coroutineScope.launch {
            _timer.value = initTime
            _isRunning.value = true
            while (isRunning.value) {
                delay(tickDelayInMillis)
                _timer.value = Time(timer.value.timeInMillis - tickDelayInMillis)
                if (timer.value.timeInMillis < tickDelayInMillis) {
                    _hasFinished.value = true
                    break
                }
            }
            _isRunning.value = false
        }
    }

    override fun stopTimer() {
        pauseTimer()
        setTimer(initialTime)
    }

    override fun pauseTimer() {
        _isRunning.value = false
        _hasFinished.value = false
        timerJob?.cancel()
    }

    override fun resumeTimer() {
        startTimer(_timer.value)
    }

    override fun resetTimer() {
        stopTimer()
    }

    override fun setTimer(startTime: Time) {
        initialTime = startTime
        _timer.value = initialTime
    }

    private companion object {
        const val DELAY_IN_MILLIS = 10L
    }
}
