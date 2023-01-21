package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainingExerciseViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle): ViewModel() {

}
