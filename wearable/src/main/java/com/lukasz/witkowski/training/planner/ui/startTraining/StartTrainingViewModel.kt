package com.lukasz.witkowski.training.planner.ui.startTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartTrainingViewModel
@Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var trainingId = ""
    var trainingTitle = ""
}