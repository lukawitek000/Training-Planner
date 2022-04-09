package com.lukasz.witkowski.training.planner.ui.currentTraining

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.time.TimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
// TODO do not navigate to the next screen after exercise timer has finished it can have more reps
// TODO is this TimerViewModel used anywhere???

@HiltViewModel
class TimerViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var millisLeft = 0L
    private var timer: CountDownTimer? = null

    private val _timeLeft = MutableLiveData(0L)
    val timeLeft: LiveData<Long>
        get() = _timeLeft

    private val _timerFinished = MutableLiveData(false)
    val timerFinished: LiveData<Boolean>
        get() = _timerFinished

    var isRunning: Boolean = false
        private set

    var isPaused: Boolean = false
        private set

    fun startTimer(time: Long) {
        timer = object : CountDownTimer(time, TimeFormatter.MILLIS_IN_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                millisLeft = millisUntilFinished
                _timeLeft.value = millisLeft
                _timerFinished.value = false
            }

            override fun onFinish() {
                timer = null
                isRunning = false
                isPaused = false
                _timerFinished.value = true
            }
        }.start()
        isRunning = true
        isPaused = false
    }

    fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        isPaused = true
    }

    fun resumeTimer() {
        startTimer(millisLeft)
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
        isRunning = false
        isPaused = false
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
    }
}
