package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toDomainExercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ExercisesListViewModel(
    private val exerciseService: ExerciseService,
    categoryController: CategoryController
) : ViewModel(), CategoryController by categoryController {

    val searchQuery: StateFlow<String>
        field = MutableStateFlow("")

    val exercises = combine(
        searchQuery.debounce(300.milliseconds).distinctUntilChanged(),
        selectedCategories
    ) { query, categories ->
        ExerciseQuery(
            query = query,
            categories = categories.map { it.toExerciseCategory() }
        )
    }.flatMapLatest { exerciseQuery ->
        exerciseService.queryExercises(exerciseQuery)
    }.map { pagingData ->
        pagingData.map { exercise -> exercise.toPresentationExercise(null) }
    }.cachedIn(viewModelScope)

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseService.deleteExercise(exercise.toDomainExercise())
        }
    }

    fun removeExerciseFromView(exercise: Exercise) {
        viewModelScope.launch {
//            val allExercises = _exercises.value.toMutableList()
//            allExercises.remove(exercise)
//            _exercises.emit(allExercises)
        }
    }

    fun undoDeleting(exercise: Exercise) {
        viewModelScope.launch {
//            val allExercises = _exercises.value.toMutableSet()
//            if (allExercises.add(exercise)) {
//                _exercises.emit(allExercises.toList())
//            }
        }
    }

    fun onSearchQueryChange(new: String) {
        searchQuery.value = new
    }
}
