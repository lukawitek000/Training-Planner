package com.lukasz.witkowski.training.planner.training.trainingOverview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.mappers.toPresentationTrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingOverviewViewModel(
    private val trainingPlanService: TrainingPlanService,
    trainingStatisticsService: TrainingStatisticsService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _trainingId = savedStateHandle.get<String>("trainingId") ?: ""
    private val trainingPlanId: TrainingPlanId
        get() = TrainingPlanId(_trainingId)

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
            val trainingPlan = domainTrainingPlan.toPresentationTrainingPlan()
            _trainingPlan.value = ResultHandler.Success(trainingPlan)
        }
    }
}