package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.currentTraining.TimerHelper
import com.lukasz.witkowski.shared.currentTraining.TrainingProgressController
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class CurrentTrainingViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val trainingRepository: TrainingRepository,
    val timerHelper: TimerHelper,
    val trainingProgressController: TrainingProgressController,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val trainingId = savedStateHandle.get<Long>("trainingId") ?: -1

    val trainingFetchState = trainingRepository.getTrainingById(trainingId = trainingId).map {
        try {
            ResultHandler.Success(it)
        } catch (e: Exception) {
            ResultHandler.Error("Training not found")
        }
    }
    val currentTrainingState = trainingProgressController.currentTrainingState

    init {
        viewModelScope.launch {
            val trainingWithExercises = trainingRepository.getTrainingByIdAsync(trainingId = trainingId)
            startTraining(trainingWithExercises)
        }
    }

    private fun startTraining(trainingWithExercises: TrainingWithExercises) {
        trainingProgressController.startTraining(trainingWithExercises)
    }


}