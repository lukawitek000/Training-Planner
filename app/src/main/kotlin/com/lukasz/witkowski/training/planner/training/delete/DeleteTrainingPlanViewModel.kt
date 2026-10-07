package com.lukasz.witkowski.training.planner.training.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.delete.DeletionEvent
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
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

    val state = trainingPlanService.getTrainingPlanById(trainingPlanId).map { plan ->
        if (plan == null) {
            _deletionEvent.send(DeletionEvent.Failure)
            DeleteTrainingPlanUiState.Loading
        } else {
            DeleteTrainingPlanUiState.LoadedTrainingPlan(plan.title)
        }
    }.catch {
        Timber.w("Failed to load training plan by id: ${it.message}")
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DeleteTrainingPlanUiState.Loading
    )

    private val _deletionEvent = Channel<DeletionEvent>()
    val deletionEvent = _deletionEvent.receiveAsFlow()

    fun deleteTrainingPlan() {
        viewModelScope.launch {
            trainingPlanService.deleteTrainingPlan(trainingPlanId)
                .onSuccess {
                    _deletionEvent.send(DeletionEvent.Success)
                }
                .onFailure {
                    _deletionEvent.send(DeletionEvent.Failure)
                }
        }
    }
}

sealed interface DeleteTrainingPlanUiState {
    data object Loading : DeleteTrainingPlanUiState
    data class LoadedTrainingPlan(val title: String) : DeleteTrainingPlanUiState
}
