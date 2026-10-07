package com.lukasz.witkowski.training.planner.training.editor

import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsViewModel
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TrainingExerciseConfigurationViewModel(
    service: ExerciseService,
    exerciseId: ExerciseId,
) : ExerciseDetailsViewModel(service, exerciseId) {
    val parameters: StateFlow<RecommendedParameters>
        field = MutableStateFlow(RecommendedParameters())
}
