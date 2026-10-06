package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditorViewModel
import com.lukasz.witkowski.training.planner.exercise.delete.DeleteExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsViewModel
import com.lukasz.witkowski.training.planner.exercise.di.exerciseModule
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.statistics.di.statisticsModule
import com.lukasz.witkowski.training.planner.training.createTraining.CreateTrainingViewModel
import com.lukasz.witkowski.training.planner.training.di.trainingModule
import com.lukasz.witkowski.training.planner.training.trainingOverview.TrainingOverviewViewModel
import com.lukasz.witkowski.training.planner.training.trainingSession.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.training.trainingsList.TrainingsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val appModule = module {
    includes(exerciseModule, trainingModule, statisticsModule)

    viewModel<ExercisesListViewModel>()
    viewModel { params ->
        ExerciseEditorViewModel(
            exerciseService = get(),
            categoryController = get(),
            exerciseId = params.getOrNull()
        )
    }
    viewModel { params ->
        ExerciseDetailsViewModel(
            service = get(),
            exerciseId = params.get()
        )
    }
    viewModel { params ->
        DeleteExerciseViewModel(
            service = get(),
            exerciseId = params.get()
        )
    }

    viewModel<TrainingsListViewModel>()
    viewModel<CreateTrainingViewModel>()
    viewModel<TrainingOverviewViewModel>()
    viewModel<TrainingSessionViewModel>()
}
