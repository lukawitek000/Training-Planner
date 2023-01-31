package com.lukasz.witkowski.training.planner.startTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartTrainingViewModel
@Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
    val trainingId = savedStateHandle.get<String>(StartTrainingActivity.TRAINING_ID_KEY)
        ?: throw IllegalStateException("Missing training id")
    val trainingTitle = savedStateHandle.get<String>(StartTrainingActivity.TRAINING_TITLE_KEY)
        ?: throw IllegalStateException("Missing training title")
}
