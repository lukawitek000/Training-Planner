package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlanMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSessionViewModel @Inject constructor(
    private val trainingPlanService: TrainingPlanService,
    private val savedStateHandle: SavedStateHandle,
    trainingSessionController: TrainingSessionController,
    timerController: TimerController
) : ViewModel(),
    TrainingSessionController by trainingSessionController,
    TimerController by timerController
{

    private val _trainingId = savedStateHandle.get<String>("trainingId")
        ?: throw Exception("Training plan id was not provided")
    private val trainingId = TrainingPlanId(_trainingId)

    init {
        fetchTrainingPlan()
        observeTrainingState()
    }

    private fun fetchTrainingPlan() {
        viewModelScope.launch {
            val trainingPlanDomain = trainingPlanService.getTrainingPlanById(trainingId)
            val trainingPlan = TrainingPlanMapper.toPresentationTrainingPlan(trainingPlanDomain)
            startTrainingSession(trainingPlan)
        }
    }

    private fun observeTrainingState() {
        viewModelScope.launch {
            trainingSessionState.collectLatest { state ->
                setTimer(state)
            }
        }
    }
    // TODO is it good approach to handle setting time
    // Separation of controllers is Interface Segregation principle (Another controller will be for calculating statistics )
    // Statistics controllers: one for healthy measurements (used by watch), second for general statistics (time etc)
    private fun setTimer(state: TrainingSessionState) {
        when (state) {
            is TrainingSessionState.ExerciseState -> setTime(state.time)
            is TrainingSessionState.RestTimeState -> {
                setTime(state.restTime)
                start()
            }
            else -> Unit
        }
    }


}
