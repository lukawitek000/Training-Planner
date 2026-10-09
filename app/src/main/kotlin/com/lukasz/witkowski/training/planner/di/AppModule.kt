package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.auth.di.authModule
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditorViewModel
import com.lukasz.witkowski.training.planner.exercise.delete.DeleteExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsViewModel
import com.lukasz.witkowski.training.planner.exercise.di.exerciseModule
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.network.networkModule
import com.lukasz.witkowski.training.planner.statistics.di.statisticsModule
import com.lukasz.witkowski.training.planner.training.delete.DeleteTrainingPlanViewModel
import com.lukasz.witkowski.training.planner.training.details.TrainingPlanDetailsViewModel
import com.lukasz.witkowski.training.planner.training.di.trainingModule
import com.lukasz.witkowski.training.planner.training.editor.TrainingExerciseConfigurationViewModel
import com.lukasz.witkowski.training.planner.training.editor.TrainingPlanEditorViewModel
import com.lukasz.witkowski.training.planner.training.list.TrainingsListViewModel
import com.lukasz.witkowski.training.planner.training.trainingSession.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.user.di.userModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val appModule = module {
    includes(networkModule, authModule, userModule, exerciseModule, trainingModule, statisticsModule)

    viewModel<ExercisesListViewModel>()
    viewModel { params ->
        ExerciseEditorViewModel(
            exerciseService = get(),
            categoryController = get(),
            exerciseId = params.getOrNull(),
        )
    }
    viewModel { params ->
        ExerciseDetailsViewModel(
            service = get(),
            exerciseId = params.get(),
        )
    }
    viewModel { params ->
        DeleteExerciseViewModel(
            service = get(),
            exerciseId = params.get(),
        )
    }

    viewModel<TrainingsListViewModel>()
    viewModel { params ->
        TrainingPlanEditorViewModel(
            trainingPlanService = get(),
            trainingPlanId = params.getOrNull(),
        )
    }
    viewModel<TrainingSessionViewModel>()
    viewModel { params ->
        TrainingPlanDetailsViewModel(
            trainingPlanService = get(),
            trainingPlanId = params.get(),
        )
    }
    viewModel { params ->
        DeleteTrainingPlanViewModel(
            trainingPlanService = get(),
            trainingPlanId = params.get(),
        )
    }
    viewModel { params ->
        TrainingExerciseConfigurationViewModel(
            service = get(),
            exerciseId = params.get(),
        )
    }
}
