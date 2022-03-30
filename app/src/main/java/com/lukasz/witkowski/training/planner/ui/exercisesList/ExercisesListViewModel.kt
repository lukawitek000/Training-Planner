package com.lukasz.witkowski.training.planner.ui.exercisesList

import androidx.compose.runtime.MutableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
import com.lukasz.witkowski.training.planner.ui.BaseCategoryFilteredListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseCategoryFilteredListViewModel() {

    init {
        fetchData()
    }

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    override fun fetchData() {
        exerciseRepository.loadExercises(selectedCategories.value)
            .onEach { _exercises.emit(it) }
            .launchIn(viewModelScope)
    }
}

