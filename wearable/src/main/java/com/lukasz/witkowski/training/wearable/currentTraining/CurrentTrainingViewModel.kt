package com.lukasz.witkowski.training.wearable.currentTraining

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CurrentTrainingViewModel : ViewModel() {

    private val _currentTrainingState = MutableLiveData<CurrentTrainingState>(CurrentTrainingState.ExerciseState)
    val currentTrainingState: LiveData<CurrentTrainingState>
        get() = _currentTrainingState

    fun navigateToTrainingExercise() {
        _currentTrainingState.value = CurrentTrainingState.ExerciseState
    }

    fun navigateToTrainingRestTime() {
        _currentTrainingState.value = CurrentTrainingState.RestTimeState
    }




}