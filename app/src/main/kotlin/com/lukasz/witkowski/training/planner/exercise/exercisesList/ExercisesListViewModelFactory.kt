package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.lukasz.witkowski.training.planner.TrainingPlannerApplication
import timber.log.Timber

class ExercisesListViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when(modelClass) {
            ExercisesListViewModel::class.java -> {
                val application = checkNotNull(extras[APPLICATION_KEY]) as? TrainingPlannerApplication
                val exerciseContainer = application!!.appContainer.exerciseContainer
                val savedStateHandle = extras.createSavedStateHandle()
                Timber.d("LWWW creation of the exercises list viewmodel")
                ExercisesListViewModel(exerciseContainer.service, exerciseContainer.categoryController, savedStateHandle)
            }
            else -> throw IllegalStateException("Unknown class $modelClass")
        } as T
    }
}
