package com.lukasz.witkowski.training.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.training.createTraining.CreateTrainingViewModel
import com.lukasz.witkowski.training.planner.training.trainingOverview.TrainingOverviewViewModel
import com.lukasz.witkowski.training.planner.training.trainingsList.TrainingsListViewModel

class TrainingPlannerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            ExercisesListViewModel::class.java -> {
                val exerciseContainer = exerciseContainer(extras)
                ExercisesListViewModel(
                    exerciseContainer.service,
                    exerciseContainer.categoryController
                )
            }
            CreateExerciseViewModel::class.java -> {
                val exerciseContainer = exerciseContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                CreateExerciseViewModel(
                    exerciseContainer.service,
                    exerciseContainer.categoriesCollection,
                    savedStateHandle
                )
            }
            EditExerciseViewModel::class.java -> {
                val exerciseContainer = exerciseContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                EditExerciseViewModel(
                    exerciseContainer.service,
                    exerciseContainer.categoriesCollection,
                    savedStateHandle
                )
            }
            TrainingsListViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                val exerciseContainer = exerciseContainer(extras)
                TrainingsListViewModel(trainingContainer.service, exerciseContainer.categoryController)
            }
            CreateTrainingViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                CreateTrainingViewModel(trainingContainer.service)
            }
            TrainingOverviewViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                val statisticsContainer = statisticsContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                TrainingOverviewViewModel(trainingContainer.service, statisticsContainer.trainingStatisticsService, savedStateHandle)
            }
            else -> throw IllegalStateException("Unknown class $modelClass")
        } as T
    }

    private fun exerciseContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.exerciseContainer

    private fun trainingContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.trainingContainer

    private fun statisticsContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.statisticsContainer

    private fun trainingPlannerApplication(extras: CreationExtras) =
        checkNotNull(extras[APPLICATION_KEY]) as TrainingPlannerApplication
}
