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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrainingSessionViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingSessionService: TrainingSessionService,
    private val trainingPlanId: TrainingPlanId
) : ViewModel() {

    private data class UserInputs(
        val exerciseKey: String? = null,
        val reps: Int? = null,
        val weightInKg: Double? = null,
        val holdDuration: Duration? = null,
        val restFeedback: SetEffort? = null,
        val summaryRpe: SessionRpe? = null,
        val summaryNotes: String = ""
    )

    private val userInputs = MutableStateFlow(UserInputs())

    val isTimerRunning: StateFlow<Boolean> = trainingSessionService.isTimerRunning
    val time: StateFlow<Time> = trainingSessionService.time

    val trainingSessionState: StateFlow<TrainingSessionState> = combine(
        trainingSessionService.trainingSessionState,
        trainingSessionService.time,
        trainingSessionService.isTimerRunning,
        userInputs
    ) { domainState, timeVal, isRunning, inputs ->
        val isSameExercise =
            when (domainState) {
                is DomainState.ExerciseState -> {
                    val totalSets = domainState.progress.currentExerciseTotalSets
                    val remainingSets = domainState.progress.remainingExerciseSets
                    val currentSet = (totalSets - remainingSets).coerceAtLeast(1)
                    val key = "${domainState.sessionExercise.exerciseId.value}_$currentSet"
                    inputs.exerciseKey == key
                }
                else -> false
            }
        domainState.toPresentationTrainingSessionState(
            trainingPlan = trainingSessionService.trainingPlan,
            time = timeVal,
            isTimerRunning = isRunning,
            selectedReps = if (isSameExercise) inputs.reps else null,
            selectedWeightInKg = if (isSameExercise) inputs.weightInKg else null,
            selectedHoldDuration = if (isSameExercise) inputs.holdDuration else null,
            selectedFeedback = inputs.restFeedback,
            summaryRpe = inputs.summaryRpe,
            summaryNotes = inputs.summaryNotes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrainingSessionState.IdleState)

    init {
        fetchTrainingPlan()
    }

    fun onIntent(intent: SessionIntent) {

    }

    fun saveStatistics() {
        val summaryState = trainingSessionState.value as? TrainingSessionState.SummaryState
        val note = summaryState?.notes?.ifEmpty { null }
        val rpe = summaryState?.selectedOverallRpe
        viewModelScope.launch {
            trainingSessionService.finishSession(note = note, rpe = rpe)
        }
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

    data object RestSkipped: SessionIntent
    data class SetFeedbackSelected(val effort: SetEffort) : SessionIntent

    data class SessionNotesChanged(val notes: String) : SessionIntent
    data class SessionFeedbackSelected(val rpe: SessionRpe) : SessionIntent
    data object SessionEnd: SessionIntent
}
