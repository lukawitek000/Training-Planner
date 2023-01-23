package com.lukasz.witkowski.training.planner.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.trainingSession.TrainingSessionActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
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
