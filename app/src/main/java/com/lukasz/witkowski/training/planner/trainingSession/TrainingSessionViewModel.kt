package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionStateConverter
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlanMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSessionViewModel @Inject constructor(
    private val trainingPlanService: TrainingPlanService,
    private val savedStateHandle: SavedStateHandle,
    private val trainingSessionService: TrainingSessionService,
    timerController: TimerController
) : ViewModel(),
    TimerController by timerController
{

    private val _trainingId = savedStateHandle.get<String>("trainingId")
        ?: throw Exception("Training plan id was not provided")
    private val trainingId = TrainingPlanId(_trainingId)

    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = trainingSessionService.trainingSessionState.map {
            TrainingSessionStateConverter.toPresentation(it)
        }.stateIn(viewModelScope, SharingStarted.Lazily, TrainingSessionState.IdleState)

    init {
        fetchTrainingPlan()
        observeTrainingState()
        observeTimer()
    }

    fun completed() {
        stopTimer()
        trainingSessionService.next(true)
    }

    fun skip() {
        stopTimer()
        trainingSessionService.next(false)
    }

    private fun fetchTrainingPlan() {
        viewModelScope.launch {
            val trainingPlanDomain = trainingPlanService.getTrainingPlanById(trainingId)
            val trainingPlan = TrainingPlanMapper.toPresentationTrainingPlan(trainingPlanDomain)
            startTrainingSession(trainingPlan)
        }
    }

    private fun startTrainingSession(trainingPlan: TrainingPlan) {
        val domainTrainingPlan = TrainingPlanMapper.toDomainTrainingPlan(trainingPlan)
        trainingSessionService.startTraining(domainTrainingPlan)
    }

    private fun observeTrainingState() {
        viewModelScope.launch {
            trainingSessionState.collectLatest { state ->
                stopTimer()
                setTimer(state.time)
                startRestTimer(state)
            }
        }
    }

    private fun startRestTimer(state: TrainingSessionState) {
        if(state is TrainingSessionState.RestTimeState) {
            startTimer()
        }
    }

    private fun observeTimer() {
        viewModelScope.launch {
            hasFinished.collectLatest {
                if (it) trainingSessionService.next()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
