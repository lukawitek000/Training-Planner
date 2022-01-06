package com.lukasz.witkowski.training.planner.currentTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.planner.repo.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentTrainingViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    var trainingWithExercises: TrainingWithExercises? = null
        private set

    fun fetchTraining(trainingId: Long) {
        viewModelScope.launch {
            trainingWithExercises = trainingRepository.fetchTrainingById(trainingId)
        }
    }
}
