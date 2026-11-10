package com.lukasz.witkowski.training.planner.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState as DomainState
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrainingSessionViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingSessionService: TrainingSessionService,
    private val trainingPlanId: TrainingPlanId
) : ViewModel() {

    val trainingSessionState: StateFlow<DomainState> = trainingSessionService.trainingSessionState

    init {
        fetchTrainingPlan()
    }

    fun onIntent(intent: SessionIntent) {
        when (intent) {
            is SessionIntent.RepsChanged -> {
                trainingSessionService.changeReps(intent.reps)
            }
            is SessionIntent.WeightChanged -> {
                trainingSessionService.changeWeight(intent.weight)
            }
            is SessionIntent.HoldTimeChanged -> {
                trainingSessionService.updateHoldTime(intent.time)
            }
            is SessionIntent.StartTimer -> {
                trainingSessionService.startTimer()
            }
            is SessionIntent.ResetTimer -> {
                trainingSessionService.resetTimer()
            }
            is SessionIntent.SetAdded -> {
                trainingSessionService.addExtraSet()
            }
            is SessionIntent.SetRemoved -> {
                trainingSessionService.removeExtraSet()
            }
            is SessionIntent.SetCompleted -> {
                trainingSessionService.completed()
            }
            is SessionIntent.SetSkipped -> {
                trainingSessionService.skip()
            }
            is SessionIntent.RestSkipped -> {
                trainingSessionService.skip()
            }
            is SessionIntent.SetFeedbackSelected -> {
                trainingSessionService.setExerciseSetFeedback(intent.effort)
            }
            is SessionIntent.SessionNotesChanged -> Unit
            is SessionIntent.SessionFeedbackSelected -> Unit
            is SessionIntent.SessionEnd -> Unit
        }
    }

    fun saveStatistics() {

    }

    private fun fetchTrainingPlan() {
        viewModelScope.launch {
            trainingPlanService.getTrainingPlanById(trainingPlanId)
                .filterNotNull()
                .first()
                .let { trainingPlan ->
                    trainingSessionService.startTraining(trainingPlan)
                    // TODO: Connect and start Foreground Service (:session-service) via SessionServiceConnector
                    // to promote the active workout session to a foreground service with ongoing notification.
                }
        }
    }

    override fun onCleared() {
        // TODO: Foreground Service Lifecycle Management:
        // When the user navigates away or backgrounding occurs, handle binding/unbinding to SessionService
        // and keep ongoing notifications active. Only call stopTraining() if the session was explicitly canceled.
    }
}

sealed interface SessionIntent {
    data class RepsChanged(val reps: Int) : SessionIntent
    data class WeightChanged(val weight: Double) : SessionIntent
    data class HoldTimeChanged(val time: Duration) : SessionIntent
    data object SetAdded : SessionIntent
    data object SetRemoved : SessionIntent
    data object SetCompleted: SessionIntent
    data object SetSkipped: SessionIntent
    data object StartTimer: SessionIntent
    data object ResetTimer: SessionIntent

    data object RestSkipped: SessionIntent
    data class SetFeedbackSelected(val effort: SetEffort) : SessionIntent

    data class SessionNotesChanged(val notes: String) : SessionIntent
    data class SessionFeedbackSelected(val rpe: SessionRpe) : SessionIntent
    data object SessionEnd: SessionIntent
}
