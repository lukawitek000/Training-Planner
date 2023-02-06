package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController

class TimerViewModel(timerController: TimerController) : ViewModel(),
    TimerController by timerController
