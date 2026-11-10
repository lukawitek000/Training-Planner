package com.lukasz.witkowski.training.planner.session.presentation.models

import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

interface TrainingSessionUiState {
    data object Idle : TrainingSessionUiState
    data object Loading : TrainingSessionUiState
    data class Exercise(
        val domainState: TrainingSessionState.ExerciseState,
        val imagesReferences: ResultHandler<List<ImageReference>>
    ) : TrainingSessionUiState

    data class RestTime(val domainState: TrainingSessionState.RestTimeState) :
        TrainingSessionUiState

    data class Summary(
        val domainState: TrainingSessionState.SummaryState,
        val trainingPlan: ResultHandler<TrainingPlan>
    ) : TrainingSessionUiState
}
