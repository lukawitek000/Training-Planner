package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toDomainExercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExercisesListViewModel(
    private val exerciseService: ExerciseService,
    categoryController: CategoryController
) : ViewModel(), CategoryController by categoryController {

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    init {
        fetchExercises()
        observeSelectedCategories()
    }

    private fun observeSelectedCategories() {
        // TODO launch in dispatchers IO (look at navigation-ui)
        viewModelScope.launch {
            selectedCategories.collectLatest {
                fetchExercises()
            }
        }
    }

    private fun fetchExercises() {
        viewModelScope.launch {
            val categories = selectedCategories.value.map { it.toExerciseCategory() }
            exerciseService.getExercisesFromCategories(categories).collectLatest {
                val exercises = it.map { exercise ->
                    val imageReference = exercise.imageId?.let { imageId ->
                        exerciseService.readImageReference(imageId)
                    }
                    exercise.toPresentationExercise(imageReference)
                }
                _exercises.emit(exercises)
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseService.deleteExercise(exercise.toDomainExercise())
        }
    }

    fun removeExerciseFromView(exercise: Exercise) {
        viewModelScope.launch {
            val allExercises = _exercises.value.toMutableList()
            allExercises.remove(exercise)
            _exercises.emit(allExercises)
        }
    }

    fun undoDeleting(exercise: Exercise) {
        viewModelScope.launch {
            val allExercises = _exercises.value.toMutableSet()
            if (allExercises.add(exercise)) {
                _exercises.emit(allExercises.toList())
            }
        }
    }
}
