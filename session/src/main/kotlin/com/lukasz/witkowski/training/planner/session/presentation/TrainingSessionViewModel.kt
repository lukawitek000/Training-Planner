package com.lukasz.witkowski.training.planner.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.presentation.toPresentationTrainingSessionState
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.time.Duration
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingSessionViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingSessionService: TrainingSessionService,
    private val sessionCompletedListener: SessionCompletedListener? = null,
    private val trainingPlanId: TrainingPlanId
) : ViewModel() {

    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = trainingSessionService.trainingSessionState.map {
            it.toPresentationTrainingSessionState()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TrainingSessionState.IdleState)
    private val currentState: TrainingSessionState
        get() = trainingSessionState.value

    val time: StateFlow<Time> = trainingSessionService.time
    val isTimerRunning: StateFlow<Boolean> = trainingSessionService.isTimerRunning

    init {
        fetchTrainingPlan()
    }

    fun completed(
        actualReps: Int? = null,
        actualWeightInKg: Double? = null,
        rpe: Float? = null,
        actualTime: Duration? = null
    ) {
        trainingSessionService.completed(actualReps, actualWeightInKg, rpe, actualTime)
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

    fun saveStatistics(note: String? = null, rpe: Int? = null) {
        viewModelScope.launch {
            (currentState as? TrainingSessionState.SummaryState)?.statistics?.let { stats ->
                val enrichedStats = stats.copy(note = note, rpe = rpe)
                sessionCompletedListener?.onSessionCompleted(enrichedStats)
            }
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
        trainingSessionService.stopTraining()
        super.onCleared()
    }
}
