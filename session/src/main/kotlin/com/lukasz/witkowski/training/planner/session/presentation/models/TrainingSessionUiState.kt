package com.lukasz.witkowski.training.planner.session.presentation.models

import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

interface TrainingSessionUiState {
    val phaseKey: TrainingSessionPhaseKey
    data object Loading : TrainingSessionUiState {
        override val phaseKey = TrainingSessionPhaseKey.Loading
    }
    data class Exercise(
        val domainState: TrainingSessionState.ExerciseState,
        val imagesReferences: ResultHandler<List<ImageReference>>
    ) : TrainingSessionUiState {
        override val phaseKey = TrainingSessionPhaseKey.Exercise(domainState.toUniqueId())

        private fun TrainingSessionState.ExerciseState.toUniqueId(): String {
            val id = sessionExercise.exerciseId
            return "Exercise_${id}"
        }
    }

    data class RestTime(val domainState: TrainingSessionState.RestTimeState) :
        TrainingSessionUiState {
        override val phaseKey = TrainingSessionPhaseKey.RestTime
    }

    data class Summary(
        val domainState: TrainingSessionState.SummaryState,
        val trainingPlan: ResultHandler<TrainingPlan>
    ) : TrainingSessionUiState {
        override val phaseKey = TrainingSessionPhaseKey.Summary
    }
}

sealed interface TrainingSessionPhaseKey {
    data object Loading : TrainingSessionPhaseKey
    data class Exercise(val exerciseSessionId: String) : TrainingSessionPhaseKey
    data object RestTime : TrainingSessionPhaseKey
    data object Summary : TrainingSessionPhaseKey
}
