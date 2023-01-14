package com.lukasz.witkowski.training.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.di.ExerciseContainer
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel

class TrainingPlannerViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when(modelClass) {
            ExercisesListViewModel::class.java -> {
                val exerciseContainer = exerciseContainer(extras)
                ExercisesListViewModel(exerciseContainer.service, exerciseContainer.categoryController)
            }
            CreateExerciseViewModel::class.java -> {
                val exerciseContainer = exerciseContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                CreateExerciseViewModel(exerciseContainer.service, exerciseContainer.categoriesCollection, savedStateHandle)
            }
            EditExerciseViewModel::class.java -> {
                val exerciseContainer = exerciseContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                EditExerciseViewModel(exerciseContainer.service, exerciseContainer.categoriesCollection, savedStateHandle)
            }
            else -> throw IllegalStateException("Unknown class $modelClass")
        } as T
    }


    private fun exerciseContainer(extras: CreationExtras): ExerciseContainer {
        val application = checkNotNull(extras[APPLICATION_KEY]) as? TrainingPlannerApplication
        return application!!.appContainer.exerciseContainer
    }
}
