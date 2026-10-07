package com.lukasz.witkowski.training.planner.training.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TrainingPlanDetailsViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingPlanId: TrainingPlanId,
) : ViewModel() {
    val trainingPlan = trainingPlanService.getTrainingPlanById(trainingPlanId).map { plan ->
        if (plan != null) {
            ResultHandler.Success(plan)
        } else {
            ResultHandler.Error("Plan not found")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ResultHandler.Loading
    )
}