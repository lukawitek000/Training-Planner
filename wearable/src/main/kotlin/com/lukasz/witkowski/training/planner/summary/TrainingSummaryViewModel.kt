package com.lukasz.witkowski.training.planner.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.trainingSession.TrainingSessionActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainingSummaryViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingStatisticsService: TrainingStatisticsService
) : ViewModel() {

    private val _trainingStatisticsId =
        savedStateHandle.get<String>(TrainingSessionActivity.TRAINING_STATISTICS_ID)
            ?: throw IllegalStateException("Missing training id")
    private val trainingStatisticsId: TrainingStatisticsId
        get() = TrainingStatisticsId(_trainingStatisticsId)

    fun loadTrainingStatistics() = trainingStatisticsService.getStatistics(trainingStatisticsId)
}
