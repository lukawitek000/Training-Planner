package com.lukasz.witkowski.training.planner.exercise.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExercise2
import com.lukasz.witkowski.training.planner.navigation.ExerciseDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExerciseDetailsViewModel(
    private val service: ExerciseService,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val exerciseId = savedStateHandle.toRoute<ExerciseDetails>().exerciseId
    val state : StateFlow<ExerciseDetailsState>
        field = MutableStateFlow<ExerciseDetailsState>(ExerciseDetailsState.Loading)

    init {
        viewModelScope.launch {
            try {
                val exercise = service.getExerciseById(exerciseId).toPresentationExercise2(null)
                state.value = ExerciseDetailsState.Success(exercise)
            } catch (e: RuntimeException) {
                state.value = ExerciseDetailsState.Failure(e.message ?: "Unknown failure")
            }
        }
    }
}

sealed interface ExerciseDetailsState {
    data object Loading: ExerciseDetailsState
    data class Success(val exercise: Exercise2): ExerciseDetailsState
    data class Failure(val message: String): ExerciseDetailsState
}