package com.lukasz.witkowski.training.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.lukasz.witkowski.training.planner.summary.TrainingSummaryViewModel
import com.lukasz.witkowski.training.planner.trainingSession.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.traininglist.TrainingPlansListViewModel

class WearableTrainingPlannerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            TrainingPlansListViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                TrainingPlansListViewModel(trainingContainer.service)
            }
            TrainingSessionViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                val statisticsContainer = statisticsContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                TrainingSessionViewModel(
                    savedStateHandle,
                    trainingContainer.service,
                    statisticsContainer.trainingSessionService,
                    statisticsContainer.trainingStatisticsService
                )
            }
            TrainingSummaryViewModel::class.java -> {
                val statisticsContainer = statisticsContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                TrainingSummaryViewModel(savedStateHandle, statisticsContainer.trainingStatisticsService)
            }
            else -> throw IllegalStateException("Unknown class $modelClass")
        } as T
    }

    private fun trainingContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.trainingContainer

    private fun statisticsContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.statisticsContainer

    private fun trainingPlannerApplication(extras: CreationExtras) =
        checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as TrainingPlannerWearableApplication
}
