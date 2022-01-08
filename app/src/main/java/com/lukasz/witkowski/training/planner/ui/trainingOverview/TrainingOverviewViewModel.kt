package com.lukasz.witkowski.training.planner.ui.trainingOverview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.models.statistics.GeneralStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class TrainingOverviewViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val statisticsRepository: StatisticsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trainingId = savedStateHandle.get<Long>("trainingId")

    private val _training = MutableStateFlow<ResultHandler<TrainingWithExercises>>(ResultHandler.Loading)
    val training: StateFlow<ResultHandler<TrainingWithExercises>> = _training

    private val _statistics = MutableStateFlow<ResultHandler<List<GeneralStatistics>>>(ResultHandler.Loading)
    val statistics: StateFlow<ResultHandler<List<GeneralStatistics>>> = _statistics

    init {
        fetchTrainingDetails()
        fetchTrainingStatistics()
    }

    private fun fetchTrainingDetails() {
        viewModelScope.launch {
            _training.value = ResultHandler.Loading
            try {
                val trainingWithExercises = trainingRepository.getTrainingById(trainingId!!)
                _training.value = ResultHandler.Success(value = trainingWithExercises)
            } catch (e: Exception) {
                _training.value = ResultHandler.Error(message = "There is no training in database")
            }
        }
    }

    private fun fetchTrainingStatistics() {
        viewModelScope.launch {
            _statistics.value = ResultHandler.Loading
            try {
                val generalStatistics = statisticsRepository.getTrainingCompleteStatisticsByTrainingId(trainingId!!)
                if (generalStatistics.isEmpty()) throw Exception()
                _statistics.value = ResultHandler.Success(value = generalStatistics)
            } catch (e: Exception) {
                _statistics.value = ResultHandler.Error(message = "There is no statistics for this training")
            }
        }
    }
}