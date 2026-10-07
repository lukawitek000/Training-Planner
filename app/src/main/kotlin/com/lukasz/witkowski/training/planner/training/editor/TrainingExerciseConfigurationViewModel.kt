package com.lukasz.witkowski.training.planner.training.editor

import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseParametersIntent
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsState
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsViewModel
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TrainingExerciseConfigurationViewModel(
    service: ExerciseService,
    exerciseId: ExerciseId,
) : ExerciseDetailsViewModel(service, exerciseId) {
    private val initialParams = RecommendedParameters()
    private val _parameters = MutableStateFlow<RecommendedParameters?>(null)

    val parameters = combine(state, _parameters) { detailsState, params ->
        if (detailsState is ExerciseDetailsState.Success && params == null) {
            detailsState.details.recommendations.first().parameters
        } else {
            params ?: initialParams
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        initialParams
    )

    fun processParametersIntent(intent: ExerciseParametersIntent) {
        when (intent) {
            is ExerciseParametersIntent.RepsChanged -> _parameters.update {
                it?.copy(reps = intent.reps) ?: RecommendedParameters(reps = intent.reps)
            }

            is ExerciseParametersIntent.SetsChanged -> _parameters.update {
                it?.copy(sets = intent.sets) ?: RecommendedParameters(sets = intent.sets)
            }

            is ExerciseParametersIntent.RestTimeChanged -> _parameters.update {
                it?.copy(restTime = intent.restTime)
                    ?: RecommendedParameters(restTime = intent.restTime)
            }

            is ExerciseParametersIntent.WeightChanged -> _parameters.update {
                it?.copy(weightInKg = intent.weight) ?: RecommendedParameters(
                    weightInKg = intent.weight
                )
            }
        }
    }

    fun recommendationSelected(recommendedParameters: RecommendedParameters) {
        _parameters.value = recommendedParameters
    }

    fun createTrainingExercise(): TrainingExercise {
        val exercise = (state.value as ExerciseDetailsState.Success).details.exercise
        return TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = exercise.toExerciseSnapshot(),
            sets = parameters.value.sets!!,
            repetitions = parameters.value.reps!!,
            restTime = parameters.value.restTime!!,
            weightInKg = parameters.value.weightInKg,
        )
    }

    private fun Exercise2.toExerciseSnapshot() = ExerciseSnapshot(
        id = id,
        name = name,
        description = description,
        categories = categories.map { ExerciseCategoryName(it.name) }.toSet()
    )
}
