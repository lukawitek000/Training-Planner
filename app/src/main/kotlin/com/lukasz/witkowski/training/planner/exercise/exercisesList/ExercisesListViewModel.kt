package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toDomainExercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExercise2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

class ExercisesListViewModel(
    private val exerciseService: ExerciseService,
    private val categoryController2: CategoryController2,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")

    val filteringState =
        combine(
            searchQuery,
            categoryController2.filterCategories
        ) { query, categories ->
            Timber.i("Query: $query, categories: $categories")
            FilteringState(
                searchQuery = query,
                categories = categories
            )
        }.stateIn(
            viewModelScope,
            initialValue = FilteringState(
                searchQuery = searchQuery.value,
                categories = emptyList()
            ),
            started = SharingStarted.WhileSubscribed(5_000L)
        )

    val exercises = filteringState.debounce(300.milliseconds).flatMapLatest { state ->
        exerciseService.queryExercises(state.toExerciseQuery())
    }.map { pagingData ->
        pagingData.map { exercise -> exercise.toPresentationExercise2(null) }
    }.cachedIn(viewModelScope)

    fun onSearchQueryChange(new: String) {
        searchQuery.value = new
    }

    fun toggleCategory(category: ExerciseCategory) {
        categoryController2.toggleCategory(category)
    }
}

data class FilteringState(
    val searchQuery: String,
    val categories: List<FilterCategory>
) {
    val isAnyCategorySelected = categories.any { it.isSelected }

    fun toExerciseQuery(): ExerciseQuery {
        val selectedCategories = categories.filter { it.isSelected }.map { it.category }
        return ExerciseQuery(
            query = searchQuery,
            categories = selectedCategories
        )
    }
}