package com.lukasz.witkowski.training.planner.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.repo.TrainingRepository
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

    private val _statisticsId = MutableStateFlow(NO_STATISTICS_ID)
    val statisticsId: StateFlow<Long> = _statisticsId

    private var trainingCompleteStatistics: TrainingCompleteStatistics? = null

    fun insertTrainingStatistics(trainingCompleteStatistics: TrainingCompleteStatistics) {
        viewModelScope.launch {
            trainingCompleteStatistics.trainingStatistics.trainingId = trainingId
            this@TrainingSummaryViewModel.trainingCompleteStatistics = trainingCompleteStatistics
            _statisticsId.value = repository.insertTrainingCompleteStatistics(trainingCompleteStatistics)
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
        return TimeFormatter.millisToTime(trainingCompleteStatistics?.trainingStatistics?.totalTime ?: 0)
    }

    companion object {
        const val NO_STATISTICS_ID = -1L
    }
}