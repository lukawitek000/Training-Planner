package com.lukasz.witkowski.training.planner.ui.trainingsList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.dummyTrainingList
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val allTrainings: StateFlow<List<Training>>
        get() = trainingRepository.getAllTrainings().stateIn(viewModelScope, SharingStarted.Lazily,emptyList<Training>())
}