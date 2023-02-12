package com.lukasz.witkowski.training.planner.training.trainingSession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionStateMapper
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingSessionViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingSessionService: TrainingSessionService,
    private val trainingStatisticsService: TrainingStatisticsService,
    timerController: TimerController,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    TimerController by timerController {

    private val _trainingId = savedStateHandle.get<String>("trainingId")
        ?: throw Exception("Training plan id was not provided")
    private val trainingId = TrainingPlanId(_trainingId)

    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = trainingSessionService.trainingSessionState.map {
            TrainingSessionStateMapper.toPresentation(it)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TrainingSessionState.IdleState)
    private val currentState: TrainingSessionState
        get() = trainingSessionState.value

    init {
        fetchTrainingPlan()
        observeTimer()
        observeTrainingSessionState()
    }

    fun completed() {
        stopTimer()
        trainingSessionService.completed()
    }

    fun skip() {
        stopTimer()
        trainingSessionService.skip()
    }

    fun saveStatistics() {
        viewModelScope.launch {
            (currentState as? TrainingSessionState.SummaryState)?.statistics?.let {
                trainingStatisticsService.save(it)
            }
        }
    }

    private fun fetchTrainingPlan() {
        viewModelScope.launch {
            val trainingPlan = trainingPlanService.getTrainingPlanById(trainingId)
            trainingSessionService.startTraining(trainingPlan)
        }
    }

    private fun observeTimer() {
        viewModelScope.launch {
            hasFinished.collectLatest {
                if (it) {
                    navigateNextIfRestTimeElapsed()
                    resetTimerIfExerciseTimeElapsed()
                }
            }
        }
    }

    private fun observeTrainingSessionState() {
        viewModelScope.launch {
            trainingSessionService.trainingSessionState.collectLatest {
                val state = TrainingSessionStateMapper.toPresentation(it)
                setTimerForRestTimeAndExercise(state)
                startRestTimeTimer(state)
            }
        }
    }

    private fun startRestTimeTimer(state: TrainingSessionState) {
        if (state is TrainingSessionState.RestTimeState) {
            startTimer()
        }
    }

    private fun setTimerForRestTimeAndExercise(state: TrainingSessionState) {
        if (state is TrainingSessionState.RestTimeState || state is TrainingSessionState.ExerciseState) {
            setTimer(state.time)
        }
    }

    private fun resetTimerIfExerciseTimeElapsed() {
        if (currentState is TrainingSessionState.ExerciseState) {
            resetTimer()
        }
    }

    private fun navigateNextIfRestTimeElapsed() {
        if (currentState is TrainingSessionState.RestTimeState) {
            skip()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        trainingSessionService.stopTraining()
    }
}
