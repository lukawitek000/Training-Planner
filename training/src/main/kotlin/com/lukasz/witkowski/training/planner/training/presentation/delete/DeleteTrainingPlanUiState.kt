package com.lukasz.witkowski.training.planner.training.presentation.delete

sealed interface DeleteTrainingPlanEvent {
    data object Success : DeleteTrainingPlanEvent
    data object Failure : DeleteTrainingPlanEvent
}

sealed interface DeleteTrainingPlanUiState {
    data object Loading : DeleteTrainingPlanUiState
    data class LoadedTrainingPlan(val title: String) : DeleteTrainingPlanUiState
    data class Error(val message: String) : DeleteTrainingPlanUiState
}
