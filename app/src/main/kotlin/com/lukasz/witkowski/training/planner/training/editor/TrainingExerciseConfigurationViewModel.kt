package com.lukasz.witkowski.training.planner.training.editor

import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.details.ExerciseDetailsState
import com.lukasz.witkowski.training.planner.exercise.presentation.details.ExerciseDetailsViewModel
import com.lukasz.witkowski.training.planner.exercise.presentation.editor.ExerciseParametersIntent
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.shared.utils.Weight
import com.lukasz.witkowski.training.planner.shared.utils.WeightUnit
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration

class TrainingExerciseConfigurationViewModel(
    service: ExerciseService,
    exerciseId: ExerciseId,
    private val userRepository: UserRepository? = null,
) : ExerciseDetailsViewModel(service, exerciseId) {
    private val weightUnit: StateFlow<WeightUnit> = userRepository?.weightUnit
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WeightUnit.KG
        )
        ?: MutableStateFlow(WeightUnit.KG)

    private val _parameters = MutableStateFlow(ParametersEditorState())

    init {
        viewModelScope.launch {
            state.collect {
                if (it is ExerciseDetailsState.Success) {
                    val recommendedParameters = it.details.recommendations.first().parameters
                    _parameters.update { current ->
                        if (current.wasModifiedByUser) {
                            current
                        } else {
                            current.copy(value = recommendedParameters)
                        }
                    }
                }
            }
        }
    }

    val parameters = _parameters.map { params -> params.value }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _parameters.value.value
    )

    fun processParametersIntent(intent: ExerciseParametersIntent) {
        when (intent) {
            is ExerciseParametersIntent.RepsChanged -> _parameters.update {
                it.copy(value = it.value.copy(reps = intent.reps), wasModifiedByUser = true)
            }

            is ExerciseParametersIntent.SetsChanged -> _parameters.update {
                it.copy(value = it.value.copy(sets = intent.sets), wasModifiedByUser = true)
            }

            is ExerciseParametersIntent.RestTimeChanged -> _parameters.update {
                it.copy(value = it.value.copy(restTime = intent.restTime), wasModifiedByUser = true)
            }

            is ExerciseParametersIntent.WeightChanged -> _parameters.update {
                val weight = intent.weight?.let { w -> Weight.of(w, weightUnit.value) }
                it.copy(value = it.value.copy(weight = weight), wasModifiedByUser = true)
            }

            is ExerciseParametersIntent.TimeChanged -> _parameters.update {
                it.copy(value = it.value.copy(time = intent.time), wasModifiedByUser = true)
            }
        }
    }

    fun recommendationSelected(recommendedParameters: RecommendedParameters) {
        _parameters.update {
            it.copy(value = recommendedParameters, wasModifiedByUser = true)
        }
    }

    fun createTrainingExercise(): TrainingExercise {
        val details = (state.value as ExerciseDetailsState.Success).details
        return TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = details.toExerciseSnapshot(),
            sets = parameters.value.sets!!,
            repetitions = parameters.value.reps!!,
            restTime = parameters.value.restTime!!,
            weight = parameters.value.weight,
            time = parameters.value.time ?: Duration.ZERO
        )
    }

    private fun ExerciseDetails.toExerciseSnapshot() = ExerciseSnapshot(
        id = exercise.id,
        name = exercise.name,
        description = exercise.description,
        categories = exercise.categories.map { ExerciseCategoryName(it.name) }.toSet(),
        imageIds = images.map { it.imageId }
    )
}

private data class ParametersEditorState(
    val value: RecommendedParameters = RecommendedParameters(),
    val wasModifiedByUser: Boolean = false,
)
