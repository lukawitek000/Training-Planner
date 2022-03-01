package com.lukasz.witkowski.training.planner.exercise.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.Category
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val exerciseService: ExerciseService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    init {
//        fetchData()
    }

    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories: StateFlow<List<Category>> = _selectedCategories

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = exerciseService.getAllExercises(selectedCategories.value).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun fetchData() {
        exerciseService.getAllExercises(selectedCategories.value)
            .onEach { _exercises.emit(it) }
            .launchIn(viewModelScope)
    }

    fun selectCategory(category: Category) {
        val list = _selectedCategories.value.toMutableList()
        if (list.contains(category)) {
            list.remove(category)
        } else {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
        fetchData()
    }
}

