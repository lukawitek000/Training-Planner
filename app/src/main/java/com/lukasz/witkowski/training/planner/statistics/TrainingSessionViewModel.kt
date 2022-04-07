package com.lukasz.witkowski.training.planner.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionController
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlanMapper
import dagger.hilt.android.lifecycle.HiltViewModel
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
    }

    private fun fetchTrainingPlan() {
        viewModelScope.launch {
            val trainingPlanDomain = trainingPlanService.getTrainingPlanById(trainingId)
            val trainingPlan = TrainingPlanMapper.toPresentationTrainingPlan(trainingPlanDomain)
            startTrainingSession(trainingPlan)
        }
    }
}
