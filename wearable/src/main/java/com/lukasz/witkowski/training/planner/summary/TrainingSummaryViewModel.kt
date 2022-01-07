package com.lukasz.witkowski.training.planner.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.training.planner.repo.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSummaryViewModel
@Inject constructor(
    private val repository: TrainingRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var trainingId = 0L

    private val _statisticsId = MutableLiveData(0L)
    val statisticsId: LiveData<Long> = _statisticsId

    fun insertTrainingStatistics(trainingCompleteStatistics: TrainingCompleteStatistics) {
        viewModelScope.launch {
            trainingCompleteStatistics.trainingStatistics.trainingId = trainingId
            _statisticsId.value = repository.insertTrainingCompleteStatistics(trainingCompleteStatistics)
        }
    }

}