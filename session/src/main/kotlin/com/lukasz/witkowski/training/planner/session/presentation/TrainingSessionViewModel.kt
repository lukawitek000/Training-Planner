package com.lukasz.witkowski.training.planner.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionUiState
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TrainingSessionViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingSessionService: TrainingSessionService,
    private val exerciseService: ExerciseService,
    private val trainingPlanId: TrainingPlanId
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val trainingSessionState: StateFlow<TrainingSessionUiState> =
        trainingSessionService.trainingSessionState.flatMapLatest { domainState ->
            when (domainState) {
                is TrainingSessionState.IdleState -> flowOf(TrainingSessionUiState.Idle)
                is TrainingSessionState.RestTimeState -> flowOf(
                    TrainingSessionUiState.RestTime(
                        domainState
                    )
                )

                is TrainingSessionState.ExerciseState -> flow {
                    emit(TrainingSessionUiState.Exercise(domainState, ResultHandler.Loading))
                    val result = runCatchingCancellable {
                        exerciseService.readImageReferences(domainState.sessionExercise.snapshot.exerciseId)
                    }
                    result.fold(
                        onSuccess = { refs ->
                            emit(
                                TrainingSessionUiState.Exercise(
                                    domainState,
                                    ResultHandler.Success(refs),
                                )
                            )
                        },
                        onFailure = {
                            emit(
                                TrainingSessionUiState.Exercise(
                                    domainState,
                                    ResultHandler.Error("Failed to load image references"),
                                )
                            )
                        }
                    )

                }

                is TrainingSessionState.SummaryState -> flow {
                    emit(TrainingSessionUiState.Summary(domainState, ResultHandler.Loading))
                    val result =
                        trainingPlanService.fetchTrainingPlanById(domainState.statistics.trainingPlanId)
                    result.fold(
                        onSuccess = {
                            emit(
                                TrainingSessionUiState.Summary(domainState, ResultHandler.Success(it))
                            )
                        },
                        onFailure = {
                            emit(
                                TrainingSessionUiState.Summary(domainState, ResultHandler.Error("Failed to load training plan"))
                            )
                        }
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TrainingSessionUiState.Loading,
        )

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

            is SessionIntent.SessionNotesChanged -> {
                trainingSessionService.updateSessionNotes(intent.notes)
            }
            is SessionIntent.SessionFeedbackSelected -> {
                trainingSessionService.updateSessionFeedback(intent.rpe)
            }
            is SessionIntent.SessionEndEarly -> {
                trainingSessionService.endEarly()
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
    data object SetCompleted : SessionIntent
    data object SetSkipped : SessionIntent
    data object StartTimer : SessionIntent
    data object ResetTimer : SessionIntent

    data object RestSkipped : SessionIntent
    data class SetFeedbackSelected(val effort: SetEffort) : SessionIntent

    data class SessionNotesChanged(val notes: String) : SessionIntent
    data class SessionFeedbackSelected(val rpe: SessionRpe) : SessionIntent

    data object SessionEndEarly : SessionIntent
}
