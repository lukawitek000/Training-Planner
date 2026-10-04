package com.lukasz.witkowski.training.planner.exercise.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

class DeleteExerciseViewModel(
    private val service: ExerciseService,
    private val exerciseId: ExerciseId
): ViewModel() {

    val state = service.getExerciseDetailsById(exerciseId).map {
        if (it == null) {
            _deletionEvent.send(DeletionEvent.Failure)
            DeleteExerciseUiState.Loading
        } else {
            DeleteExerciseUiState.LoadedExercise(it.exercise.name)
        }
    }.catch {
        Timber.w("Failed to load exercise details by id: ${it.message}")
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DeleteExerciseUiState.Loading
    )

    private val _deletionEvent = Channel<DeletionEvent>()
    val deletionEvent = _deletionEvent.receiveAsFlow()

    fun deleteExercise() {
        viewModelScope.launch {
            runCatching {
                service.deleteExercise(exerciseId)
                _deletionEvent.send(DeletionEvent.Success)
            }.onFailure {
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
}