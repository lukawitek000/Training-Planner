package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditorViewModel
import com.lukasz.witkowski.training.planner.exercise.delete.DeleteExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsViewModel
import com.lukasz.witkowski.training.planner.exercise.di.exerciseModule
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import org.koin.core.context.GlobalContext.get
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val appModule = module {
    includes(exerciseModule)

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
}
