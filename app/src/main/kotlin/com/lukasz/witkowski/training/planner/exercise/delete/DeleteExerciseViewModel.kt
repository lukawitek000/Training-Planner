package com.lukasz.witkowski.training.planner.exercise.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class DeleteExerciseViewModel(
    private val service: ExerciseService,
    private val exerciseId: ExerciseId
): ViewModel() {

    private var isDeleting = false

    val state: StateFlow<DeleteExerciseUiState> = service.getExerciseDetailsById(exerciseId).map {
        if (it == null) {
            if (!isDeleting) {
                DeleteExerciseUiState.Error("Exercise not found")
            } else {
                DeleteExerciseUiState.Loading
            }
        } else {
            DeleteExerciseUiState.LoadedExercise(it.exercise.name)
        }
    }.catch {
        Timber.w("Failed to load exercise details by id: ${it.message}")
        emit(DeleteExerciseUiState.Error(it.message ?: "Failed to load exercise"))
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DeleteExerciseUiState.Loading
    )

    private val _deletionEvent = Channel<DeletionEvent>()
    val deletionEvent = _deletionEvent.receiveAsFlow()

    fun deleteExercise() {
        if (isDeleting) return
        isDeleting = true
        viewModelScope.launch {
            runCatching {
                service.deleteExercise(exerciseId)
                _deletionEvent.send(DeletionEvent.Success)
            }.onFailure {
                isDeleting = false
                _deletionEvent.send(DeletionEvent.Failure)
            }
        }
    }
}

sealed interface DeletionEvent {
    data object Success : DeletionEvent
    data object Failure : DeletionEvent
}

sealed interface DeleteExerciseUiState {
    data object Loading: DeleteExerciseUiState
    data class LoadedExercise(val exerciseName: String): DeleteExerciseUiState
    data class Error(val message: String): DeleteExerciseUiState
}
