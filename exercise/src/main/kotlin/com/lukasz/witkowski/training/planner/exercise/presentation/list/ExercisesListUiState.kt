package com.lukasz.witkowski.training.planner.exercise.presentation.list

import com.lukasz.witkowski.training.planner.exercise.presentation.FilteringState
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseOverview

sealed interface ExercisesListUiState {
    val filteringState: FilteringState

    data class Loading(override val filteringState: FilteringState) : ExercisesListUiState
    data class Success(
        override val filteringState: FilteringState,
        val exercises: List<ExerciseOverview>
    ) : ExercisesListUiState

    data class Failure(override val filteringState: FilteringState, val message: String?) :
        ExercisesListUiState
}
