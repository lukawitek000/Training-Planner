package com.lukasz.witkowski.training.planner.training.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import com.lukasz.witkowski.training.planner.training.domain.TrainingQuery
import com.lukasz.witkowski.training.planner.ui.components.FilteringState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.toSet
import kotlin.time.Duration.Companion.milliseconds

class TrainingsListViewModel(
    private val service: TrainingPlanService,
    private val categoryController: CategoryController2
) : ViewModel() {

    private val query = MutableStateFlow("")
    val uiState: StateFlow<TrainingsListUiState> =
        combine(query, categoryController.filterCategories) { searchQuery, filterCategories ->
            FilteringState(searchQuery, filterCategories)
        }
            .debounce(300.milliseconds)
            .flatMapLatest { filteringState ->
                val trainingQuery = filteringState.toTrainingQuery()
                service.getTrainingPlansOverviews(trainingQuery)
                    .map<List<TrainingPlanOverview>, TrainingsListUiState> {
                        TrainingsListUiState.Success(filteringState, it)
                    }.catch {
                        emit(
                            TrainingsListUiState.Failure(
                                filteringState,
                                it.message
                            )
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = TrainingsListUiState.Loading(
                    FilteringState(query.value, emptyList())
                )
            )

    fun onSearchQueryChange(new: String) {
        query.value = new
    }

    fun toggleCategory(category: ExerciseCategory) {
        categoryController.toggleCategory(category)
    }

    private fun FilteringState.toTrainingQuery(): TrainingQuery {
        val categoryNames = categories.filter { it.isSelected }
            .map { ExerciseCategoryName(it.category.name) }.toSet()
        return TrainingQuery(searchQuery, categoryNames)
    }
}

sealed interface TrainingsListUiState {
    val filteringState: FilteringState

    data class Loading(override val filteringState: FilteringState) : TrainingsListUiState
    data class Success(
        override val filteringState: FilteringState,
        val plans: List<TrainingPlanOverview>
    ) :
        TrainingsListUiState

    data class Failure(override val filteringState: FilteringState, val message: String?) :
        TrainingsListUiState
}
