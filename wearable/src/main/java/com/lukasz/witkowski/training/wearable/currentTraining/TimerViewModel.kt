package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.utils.TimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimerViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel(){

    private var millisLeft = 0L
    private var timer : CountDownTimer? = null

    private val _timeLeft = MutableLiveData(0L)
    val timeLeft: LiveData<Long>
        get() = _timeLeft

    var isRunning: Boolean = false
        private set

    var isPaused: Boolean = false
        private set


    fun startTimer(time: Long) {
        timer = object : CountDownTimer(time, TimeFormatter.MILLIS_IN_SECONDS) {
            override fun onTick(millisUntilFinished: Long) {
                millisLeft = millisUntilFinished
                _timeLeft.value = millisLeft
            }

            override fun onFinish() {
                timer = null
                isRunning = false
                isPaused = false
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
        Timber.d("On cleared timer view modedl")
    }

    init {
        Timber.d("Init timer view model")
    }

}
