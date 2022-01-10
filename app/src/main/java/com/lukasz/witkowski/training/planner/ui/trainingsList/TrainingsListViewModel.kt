package com.lukasz.witkowski.training.planner.ui.trainingsList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import com.lukasz.witkowski.training.planner.ui.BaseCategoryFilteredListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseCategoryFilteredListViewModel() {

    init {
        fetchData()
    }

    private val _trainings = MutableStateFlow<List<TrainingWithExercises>>(emptyList())
    val trainings: StateFlow<List<TrainingWithExercises>> = _trainings


    override fun fetchData() {
        trainingRepository.loadTrainingsWithExercises(selectedCategories.value)
            .onEach {
                Timber.d(" ${it.size} Fetched list of trainings $it")
                it.forEach { training ->
                    Timber.d(" ${training.exercises} Fetched training $training")
                }
                _trainings.emit(it)
            }
            .launchIn(viewModelScope)
    }

}
