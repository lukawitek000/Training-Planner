package com.lukasz.witkowski.training.planner.statistics.domain.timer

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

class Timer(
    private val tickDelayInMillis: Long = DELAY_IN_MILLIS,
    timerDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(timerDispatcher + CoroutineName("Timer"))
    private var timerJob: Job? = null
    private var initialTime = Time.ZERO

    private val _time = MutableStateFlow(Time.ZERO)
    val time: StateFlow<Time>
        get() = _time

    private val _hasFinished = MutableStateFlow(false)
    val hasFinished: StateFlow<Boolean>
        get() = _hasFinished
    
    
    private var _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean>
    get() = _isRunning

    fun setTime(startTime: Time) {
        initialTime = startTime
        _time.value = initialTime
    }

    fun start() {
        start(initialTime)
    }

    fun stop() {
        pause()
        setTime(initialTime)
    }

    fun resume() {
        start(_time.value)
    }

    fun pause() {
        _isRunning.value = false
        _hasFinished.value = false
        timerJob?.cancel()
    }



    private fun start(initTime: Time) {
        timerJob = coroutineScope.launch {
            _time.value = initTime
            _isRunning.value = true
            while (_isRunning.value) {
                delay(tickDelayInMillis)
                _time.value = Time(time.value.timeInMillis - tickDelayInMillis)
                if (time.value.timeInMillis < tickDelayInMillis) {
                    _hasFinished.value = true
                    break
                }
            }
            _isRunning.value = false
        }
    }

    private companion object {
        const val DELAY_IN_MILLIS = 10L
    }
}
