package com.lukasz.witkowski.training.wearable.currentTraining

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CurrentTrainingViewModel
@Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _currentTrainingState =
        MutableLiveData<CurrentTrainingState>(CurrentTrainingState.ExerciseState)
    val currentTrainingState: LiveData<CurrentTrainingState>
        get() = _currentTrainingState

    fun navigateToTrainingExercise() {
        _currentTrainingState.value = CurrentTrainingState.ExerciseState
    }

    fun navigateToTrainingRestTime() {
        _currentTrainingState.value = CurrentTrainingState.RestTimeState
    }

    fun navigateToTrainingSummary() {
        _currentTrainingState.value = CurrentTrainingState.SummaryState
    }


}