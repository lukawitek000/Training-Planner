package com.lukasz.witkowski.training.planner.training.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.delete.DeletionEvent
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class DeleteTrainingPlanViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingPlanId: TrainingPlanId
) : ViewModel() {

    private var isDeleting = false

    val state: StateFlow<DeleteTrainingPlanUiState> = trainingPlanService.getTrainingPlanById(trainingPlanId).map { plan ->
        if (plan == null) {
            if (!isDeleting) {
                DeleteTrainingPlanUiState.Error("Training plan not found")
            } else {
                DeleteTrainingPlanUiState.Loading
            }
        } else {
            DeleteTrainingPlanUiState.LoadedTrainingPlan(plan.title)
        }
    }.catch {
        Timber.w("Failed to load training plan by id: ${it.message}")
        emit(DeleteTrainingPlanUiState.Error(it.message ?: "Failed to load training plan"))
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DeleteTrainingPlanUiState.Loading
    )

    private val _deletionEvent = Channel<DeletionEvent>()
    val deletionEvent = _deletionEvent.receiveAsFlow()

    fun deleteTrainingPlan() {
        if (isDeleting) return
        isDeleting = true
        viewModelScope.launch {
            trainingPlanService.deleteTrainingPlan(trainingPlanId)
                .onSuccess {
                    _deletionEvent.send(DeletionEvent.Success)
                }
                .onFailure {
                    isDeleting = false
                    _deletionEvent.send(DeletionEvent.Failure)
                }
        }
    }
}

sealed interface DeleteTrainingPlanUiState {
    data object Loading : DeleteTrainingPlanUiState
    data class LoadedTrainingPlan(val title: String) : DeleteTrainingPlanUiState
    data class Error(val message: String) : DeleteTrainingPlanUiState
}
