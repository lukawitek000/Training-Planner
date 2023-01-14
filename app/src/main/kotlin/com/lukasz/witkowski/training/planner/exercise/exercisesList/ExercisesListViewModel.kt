package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject internal constructor(
    private val exerciseService: ExerciseService,
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
        // TODO launch in dispatchers IO (look at navigation-ui)
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
                val exercises = it.map { exercise ->
                    val imageReference = exercise.imageId?.let { imageId ->
                        exerciseService.readImageReference(imageId)
                    }
                    ExerciseMapper.toPresentationExercise(exercise, imageReference)
                }
                _exercises.emit(exercises)
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            val domainExercise = ExerciseMapper.toDomainExercise(exercise)
            exerciseService.deleteExercise(domainExercise)
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
            if(allExercises.add(exercise)) {
                _exercises.emit(allExercises.toList())
            }
        }
    }
}
