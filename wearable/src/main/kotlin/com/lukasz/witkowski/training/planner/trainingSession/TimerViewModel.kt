package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import kotlinx.coroutines.launch

class TimerViewModel(timerController: TimerController) : ViewModel(),
    TimerController by timerController {

    private val _timerController = MutableLiveData<TimerController>()
    val timerController: LiveData<TimerController>
        get() = _timerController

    fun provideTimerController(provide: suspend () -> TimerController) {
        viewModelScope.launch {
            _timerController.value = provide()
        }
    }
}
