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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSessionViewModel @Inject constructor(
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

    private val _trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)
    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = _trainingSessionState
    private val currentState: TrainingSessionState
        get() = trainingSessionState.value

    init {
        fetchTrainingPlan()
        observeTimer()
        observeTrainingSessionState()
    }

    fun completed() {
        stopTimer()
        val state = trainingSessionService.completed()
        _trainingSessionState.value = TrainingSessionStateMapper.toPresentation(state)
    }

    fun skip() {
        stopTimer()
        val state = trainingSessionService.skip()
        _trainingSessionState.value = TrainingSessionStateMapper.toPresentation(state)
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
            // TODO maybe it should be moved to the service??
            val trainingPlan = trainingPlanService.getTrainingPlanById(trainingId)
            val state = trainingSessionService.startTraining(trainingPlan)
            _trainingSessionState.value = TrainingSessionStateMapper.toPresentation(state)
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
            trainingSessionState.collect {
                setTimerForRestTimeAndExercise(it)
                startRestTimeTimer(it)
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
