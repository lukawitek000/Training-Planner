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

    fun onRepsChanged(reps: Int) {
        val current = trainingSessionState.value as? TrainingSessionState.ExerciseState ?: return
        userInputs.update {
            it.copy(
                exerciseKey = "${current.currentExercise.id.value}_${current.currentSet}",
                reps = reps,
                weightInKg = it.weightInKg ?: current.selectedWeightInKg,
                holdDuration = it.holdDuration ?: current.selectedHoldDuration
            )
        }
    }

    fun onWeightChanged(weight: Double) {
        val current = trainingSessionState.value as? TrainingSessionState.ExerciseState ?: return
        userInputs.update {
            it.copy(
                exerciseKey = "${current.currentExercise.id.value}_${current.currentSet}",
                weightInKg = weight,
                reps = it.reps ?: current.selectedReps,
                holdDuration = it.holdDuration ?: current.selectedHoldDuration
            )
        }
    }

    fun onHoldDurationChanged(duration: Duration) {
        val current = trainingSessionState.value as? TrainingSessionState.ExerciseState ?: return
        userInputs.update {
            it.copy(
                exerciseKey = "${current.currentExercise.id.value}_${current.currentSet}",
                holdDuration = duration,
                reps = it.reps ?: current.selectedReps,
                weightInKg = it.weightInKg ?: current.selectedWeightInKg
            )
        }
    }

    fun toggleHoldTimer() {
        if (isTimerRunning.value) {
            trainingSessionService.pauseTimer()
        } else {
            trainingSessionService.startTimer()
        }
    }

    fun resetHoldTimer() {
        trainingSessionService.resetTimer()
    }

    fun completed() {
        val current = trainingSessionState.value as? TrainingSessionState.ExerciseState
        val reps = current?.selectedReps
        val weight = current?.selectedWeightInKg
        val time = if (current?.isIsometric == true) current.selectedHoldDuration else null
        trainingSessionService.completed(
            actualReps = reps,
            actualWeightInKg = weight,
            actualTime = time
        )
    }

    fun skip() {
        trainingSessionService.skip()
    }

    fun skipExercise() {
        trainingSessionService.skipExercise()
    }

    fun addExtraSet() {
        trainingSessionService.addExtraSet()
    }

    fun onFeedbackSelected(effort: SetEffort) {
        userInputs.update { it.copy(restFeedback = effort) }
        trainingSessionService.updateLastAttemptEffort(effort)
    }

    fun onSummaryRpeSelected(rpe: SessionRpe) {
        userInputs.update { it.copy(summaryRpe = rpe) }
    }

    fun onSummaryNotesChanged(notes: String) {
        userInputs.update { it.copy(summaryNotes = notes) }
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

    fun startTimer() {
        trainingSessionService.startTimer()
    }

    fun pauseTimer() {
        trainingSessionService.pauseTimer()
    }

    fun stopTimer() {
        trainingSessionService.stopTimer()
    }

    override fun onCleared() {
        // TODO: Foreground Service Lifecycle Management:
        // When the user navigates away or backgrounding occurs, handle binding/unbinding to SessionService
        // and keep ongoing notifications active. Only call stopTraining() if the session was explicitly canceled.
    }
}
