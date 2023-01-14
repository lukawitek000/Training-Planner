package com.lukasz.witkowski.training.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.lukasz.witkowski.training.planner.traininglist.TrainingPlansListViewModel

class WearableTrainingPlannerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            TrainingPlansListViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                TrainingPlansListViewModel(trainingContainer.service)
            }
            else -> throw IllegalStateException("Unknown class $modelClass")
        } as T
    }

    private fun trainingContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.trainingContainer

    private fun trainingPlannerApplication(extras: CreationExtras) =
        checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as TrainingPlannerWearableApplication
}
