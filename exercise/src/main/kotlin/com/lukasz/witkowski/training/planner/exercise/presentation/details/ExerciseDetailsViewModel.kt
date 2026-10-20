package com.lukasz.witkowski.training.planner.exercise.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExerciseDetails
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ExerciseDetailsViewModel(
    private val service: ExerciseService,
    private val exerciseId: ExerciseId,
    private val syncRepository: ExerciseSyncRepository? = null,
    private val syncScheduler: SyncScheduler? = null
) : ViewModel() {

    private val _conflictServerDetails = MutableStateFlow<ExerciseDetails?>(null)
    val conflictServerDetails: StateFlow<ExerciseDetails?> = _conflictServerDetails.asStateFlow()

    private val _showConflictDialog = MutableStateFlow(false)
    val showConflictDialog: StateFlow<Boolean> = _showConflictDialog.asStateFlow()

    val state: StateFlow<ExerciseDetailsState> = service.getExerciseDetailsById(exerciseId)
        .map { domainDetails ->
            if (domainDetails == null) {
                ExerciseDetailsState.Failure("Exercise not found")
            } else {
                if (domainDetails.exercise.metadata.syncState == ExerciseSyncState.CONFLICT) {
                    loadConflictServerDetails()
                }
                ExerciseDetailsState.Success(domainDetails.toPresentationExerciseDetails())
            }
        }
        .catch {
            emit(ExerciseDetailsState.Failure(it.message ?: "Unknown failure"))
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            initialValue = ExerciseDetailsState.Loading
        )

    fun openConflictDialog() {
        loadConflictServerDetails()
        _showConflictDialog.value = true
    }

    fun dismissConflictDialog() {
        _showConflictDialog.value = false
    }

    fun resolveConflictKeepLocal() {
        val repo = syncRepository ?: return
        viewModelScope.launch {
            repo.resolveConflictKeepLocal(exerciseId)
            _showConflictDialog.value = false
            syncScheduler?.enqueueMutationSync()
        }
    }

    fun resolveConflictAcceptServer() {
        val repo = syncRepository ?: return
        viewModelScope.launch {
            repo.resolveConflictAcceptServer(exerciseId)
            _showConflictDialog.value = false
        }
    }

    fun retrySync() {
        syncScheduler?.enqueueMutationSync()
    }

    private fun loadConflictServerDetails() {
        val repo = syncRepository ?: return
        viewModelScope.launch {
            when (val result = repo.getConflictServerExercise(exerciseId)) {
                is AppResult.Success -> {
                    _conflictServerDetails.value = result.value.toPresentationExerciseDetails()
                }
                is AppResult.Error -> Unit
            }
        }
    }
}

sealed interface ExerciseDetailsState {
    data object Loading : ExerciseDetailsState
    data class Success(val details: ExerciseDetails) : ExerciseDetailsState
    data class Failure(val message: String) : ExerciseDetailsState
}
