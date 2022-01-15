package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CurrentTrainingViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val trainingId = savedStateHandle.get<Long>("trainingId") ?: -1


}