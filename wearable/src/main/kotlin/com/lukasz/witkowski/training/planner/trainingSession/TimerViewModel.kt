package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    timerController: TimerController
) : ViewModel(), TimerController by timerController
