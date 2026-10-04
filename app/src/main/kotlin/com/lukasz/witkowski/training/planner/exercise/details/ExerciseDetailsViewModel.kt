package com.lukasz.witkowski.training.planner.exercise.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExerciseDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails as DomainExerciseDetails

class ExerciseDetailsViewModel(
    private val service: ExerciseService,
    private val exerciseId: ExerciseId,
) : ViewModel() {
    val state: StateFlow<ExerciseDetailsState> = service.getExerciseDetailsById(exerciseId)
        .map {
            if (it == null) {
                ExerciseDetailsState.Failure("Exercise not found")
            } else {
                ExerciseDetailsState.Success(it.toPresentationExerciseDetails())
            }
        }
        .catch {
            emit(ExerciseDetailsState.Failure(it.message ?: "Unknown failure"))
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            initialValue = ExerciseDetailsState.Loading
        )
}

sealed interface ExerciseDetailsState {
    data object Loading : ExerciseDetailsState
    data class Success(val details: ExerciseDetails) : ExerciseDetailsState
    data class Failure(val message: String) : ExerciseDetailsState
}