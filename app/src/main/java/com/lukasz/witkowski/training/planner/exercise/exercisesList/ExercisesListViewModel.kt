package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.CategoryService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO What do you think about using CategoryController to handle category filtering?
// It will be reused in Training List
@HiltViewModel
class ExercisesListViewModel @Inject internal constructor(
    private val exerciseService: ExerciseService,
    private val categoryService: CategoryService,
    categoryController: CategoryController,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), CategoryController by categoryController {

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    init {
        fetchExercises()
        observeSelectedCategories()
    }

    private fun observeSelectedCategories() {
        viewModelScope.launch {
            selectedCategories.collectLatest {
                fetchExercises()
            }
        }
    }

    private fun fetchExercises() {
        viewModelScope.launch {
            val categories = selectedCategories.value.map { CategoryMapper.toExerciseCategory(it) }
            exerciseService.getExercisesFromCategories(categories).collectLatest {
                _exercises.emit(ExerciseMapper.toPresentationExercises(it))
            }
        }
    }
}
