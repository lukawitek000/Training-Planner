package com.lukasz.witkowski.training.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController
import com.lukasz.witkowski.training.planner.training.createTraining.CreateTrainingViewModel
import com.lukasz.witkowski.training.planner.training.trainingOverview.TrainingOverviewViewModel
import com.lukasz.witkowski.training.planner.training.trainingSession.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.training.trainingsList.TrainingsListViewModel

class TrainingPlannerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            TrainingsListViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                val categoriesCollection: CategoriesCollection by lazy { DefaultCategoriesCollection() }
                val categoryController: CategoryController by lazy {
                    DefaultCategoryController(
                        categoriesCollection,
                    )
                }
                TrainingsListViewModel(
                    trainingContainer.service,
                    categoryController
                )
            }

            CreateTrainingViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                CreateTrainingViewModel(trainingContainer.service)
            }

            TrainingOverviewViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                val statisticsContainer = statisticsContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                TrainingOverviewViewModel(
                    trainingContainer.service,
                    statisticsContainer.trainingStatisticsService,
                    savedStateHandle
                )
            }

            TrainingSessionViewModel::class.java -> {
                val trainingContainer = trainingContainer(extras)
                val statisticsContainer = statisticsContainer(extras)
                val savedStateHandle = extras.createSavedStateHandle()
                TrainingSessionViewModel(
                    trainingContainer.service,
                    statisticsContainer.trainingSessionService,
                    statisticsContainer.trainingStatisticsService,
                    savedStateHandle
                )
            }

            else -> throw IllegalStateException("Unknown class $modelClass")
        } as T
    }

    private fun trainingContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.trainingContainer

    private fun statisticsContainer(extras: CreationExtras) =
        trainingPlannerApplication(extras).appContainer.statisticsContainer

    private fun trainingPlannerApplication(extras: CreationExtras) =
        checkNotNull(extras[APPLICATION_KEY]) as TrainingPlannerApplication
}
