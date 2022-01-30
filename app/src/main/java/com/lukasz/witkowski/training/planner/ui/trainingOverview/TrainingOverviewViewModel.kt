package com.lukasz.witkowski.training.planner.ui.trainingOverview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.models.statistics.GeneralStatistics
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class TrainingOverviewViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val statisticsRepository: StatisticsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trainingId = savedStateHandle.get<Long>("trainingId") ?: -1

    private val _training = trainingRepository.getTrainingById(trainingId)
    val training: Flow<TrainingWithExercises> = _training

    private val _statistics =
        statisticsRepository.getTrainingCompleteStatisticsByTrainingId(trainingId)
    val statistics: Flow<List<GeneralStatistics>> = _statistics
}