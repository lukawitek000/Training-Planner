package com.lukasz.witkowski.training.planner.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSummaryViewModel
@Inject constructor(
    private val repository: TrainingRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var trainingId = 0L

    private val _insertStatisticsState =
        MutableStateFlow<ResultHandler<Long>>(ResultHandler.Loading)
    val insertStatisticsState: StateFlow<ResultHandler<Long>> = _insertStatisticsState

    private var trainingCompleteStatistics: TrainingCompleteStatistics? = null

    fun insertTrainingStatistics(trainingCompleteStatistics: TrainingCompleteStatistics) {
        viewModelScope.launch {
            trainingCompleteStatistics.trainingStatistics.trainingId = trainingId
            this@TrainingSummaryViewModel.trainingCompleteStatistics = trainingCompleteStatistics
            try {
                val statisticsId =
                    repository.insertTrainingCompleteStatistics(trainingCompleteStatistics)
                _insertStatisticsState.value = ResultHandler.Success(statisticsId)
            } catch (e: Exception) {
                _insertStatisticsState.value = ResultHandler.Error(message = "Inserting to database failed")
            }

        }
    }

    fun calculateMaxHeartRate(): Double {
        val maxHeartRate = trainingCompleteStatistics?.exercisesStatistics?.maxByOrNull {
            it.heartRateStatistics.max
        }?.heartRateStatistics?.max ?: 0.0
        return maxHeartRate
    }

    fun calculateTotalBurnedCalories(): Double {
        var totalBurnedCalories = 0.0
        trainingCompleteStatistics?.exercisesStatistics?.forEach {
            totalBurnedCalories += it.burntCaloriesStatistics.burntCalories
        }
        return totalBurnedCalories
    }

    fun getTrainingTotalTime(): String {
        return TimeFormatter.millisToTime(
            trainingCompleteStatistics?.trainingStatistics?.totalTime ?: 0
        )
    }
}
