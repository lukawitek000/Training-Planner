package com.lukasz.witkowski.training.planner.ui.trainingOverview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlanMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@HiltViewModel
class TrainingOverviewViewModel @Inject constructor(
    private val trainingPlanService: TrainingPlanService,
    private val statisticsRepository: StatisticsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trainingId = savedStateHandle.get<String>("trainingId") ?: ""

    private val _training = trainingPlanService.getTrainingPlansFromCategories(emptyList()).map { it.first { it.id.value == trainingId } } // TODO temporary fix
    val training: Flow<TrainingPlan> = _training.map { TrainingPlanMapper.toPresentationTrainingPlan(it) }

//    private val _statistics =
//        statisticsRepository.getTrainingCompleteStatisticsByTrainingId(trainingId)
//    val statistics: Flow<List<GeneralStatistics>> = _statistics
}