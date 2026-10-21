package com.lukasz.witkowski.training.planner.exercise.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilteringState
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExercise2
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ExercisesListViewModel(
    private val exerciseService: ExerciseService,
    private val categoryController2: CategoryController2,
    private val syncScheduler: SyncScheduler
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")

    init {
        syncScheduler.enqueueRemoteChangeSync(force = false)
    }

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
            initialValue =
            FilteringState(
                searchQuery = searchQuery.value,
                categories = emptyList()
            ),
            started = SharingStarted.WhileSubscribed(5_000L)
        )

    val exercises =
        filteringState
            .debounce(300.milliseconds)
            .flatMapLatest { state ->
                exerciseService.queryExercises(state.toExerciseQuery())
            }.map { pagingData ->
                pagingData.map { exercise ->
                    val imageReferences = if (exercise.imageIds.isNotEmpty()) {
                        exerciseService.readImageReferences(exercise.id)
                    } else {
                        emptyList()
                    }
                    exercise.toPresentationExercise2(imageReferences)
                }
            }.cachedIn(viewModelScope)

    fun onSearchQueryChange(new: String) {
        searchQuery.value = new
    }

    fun toggleCategory(category: ExerciseCategory) {
        categoryController2.toggleCategory(category)
    }

    fun clearCategories() {
        categoryController2.selectCategories(emptySet())
    }

    fun resetFilters() {
        searchQuery.value = ""
        categoryController2.selectCategories(emptySet())
    }

    private fun FilteringState.toExerciseQuery(): ExerciseQuery {
        val selectedCategories = categories.filter { it.isSelected }.map { it.category }
        return ExerciseQuery(
            query = searchQuery,
            categories = selectedCategories
        )
    }
}
