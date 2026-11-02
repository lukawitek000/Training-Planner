package com.lukasz.witkowski.training.planner.training.presentation.list

import com.lukasz.witkowski.training.planner.exercise.presentation.FilteringState
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview

sealed interface TrainingsListUiState {
    val filteringState: FilteringState

    data class Loading(override val filteringState: FilteringState) : TrainingsListUiState
    data class Success(
        override val filteringState: FilteringState,
        val plans: List<TrainingPlanOverview>
    ) : TrainingsListUiState

    data class Failure(override val filteringState: FilteringState, val message: String?) :
        TrainingsListUiState
}
