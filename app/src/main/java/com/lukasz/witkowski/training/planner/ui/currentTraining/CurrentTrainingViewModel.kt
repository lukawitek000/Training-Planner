package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class CurrentTrainingViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val trainingRepository: TrainingRepository,
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




}