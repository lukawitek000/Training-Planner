package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.CategoryService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.models.Category
import com.lukasz.witkowski.training.planner.exercise.models.CategoryMapper
import com.lukasz.witkowski.training.planner.exercise.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.models.ExerciseMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject internal constructor(
    private val exerciseService: ExerciseService,
    private val categoryService: CategoryService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val allCategories = categoryService.getAllCategoriesWithoutNone().map { CategoryMapper.toCategory(it) }
    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories: StateFlow<List<Category>> = _selectedCategories

    // TODO How to handle flows Flow to StateFlow etc.??

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    init {
        fetchExercises()
    }

    // TODO Is it good place for filtering?? (Service) ViewModel trzymać w małym rozmiarze
    private fun fetchExercises() {
        val fetchedExercises = if (selectedCategories.value.isEmpty()) {
            exerciseService.getAllExercises()
        } else {
            val exerciseCategories =
                selectedCategories.value.map { CategoryMapper.toExerciseCategory(it) }
            exerciseService.getExercisesFromCategories(exerciseCategories)
        }
        fetchedExercises
            .onEach {
                _exercises.emit(it.map { exercise ->
                    ExerciseMapper.toPresentationExercise(
                        exercise
                    )
                })
            }
            .launchIn(viewModelScope)
    }

    fun selectCategory(category: Category) {
        val list = _selectedCategories.value.toMutableList()
        if (!list.remove(category)) {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
        fetchExercises()
    }
}

