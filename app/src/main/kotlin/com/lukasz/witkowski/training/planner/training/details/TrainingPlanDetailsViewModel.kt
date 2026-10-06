package com.lukasz.witkowski.training.planner.training.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lukasz.witkowski.training.planner.navigation.TrainingPlanDetails
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingPlanDetailsViewModel(
    private val trainingPlanService: TrainingPlanService,
    trainingStatisticsService: TrainingStatisticsService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val trainingPlanId = savedStateHandle.toRoute<TrainingPlanDetails>().trainingPlanId

    private val _trainingPlan = MutableStateFlow<ResultHandler<TrainingPlan>>(ResultHandler.Idle)
    val trainingPlan: StateFlow<ResultHandler<TrainingPlan>>
        get() = _trainingPlan

    val trainingStatistics: StateFlow<List<TrainingStatistics>> =
        trainingStatisticsService.getStatistics(trainingPlanId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        fetchTrainingPlan()
    }

    private fun fetchTrainingPlan() {
        viewModelScope.launch {
            _trainingPlan.value = ResultHandler.Loading
            val domainTrainingPlan =
                trainingPlanService.getTrainingPlanById(trainingPlanId = trainingPlanId)
//            val trainingPlan = domainTrainingPlan.toPresentationTrainingPlan()
//            _trainingPlan.value = ResultHandler.Success(trainingPlan)
        }
    }
}