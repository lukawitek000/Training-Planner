package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.BuildConfig
import com.lukasz.witkowski.training.planner.auth.di.authModule
import com.lukasz.witkowski.training.planner.exercise.di.exerciseModule
import com.lukasz.witkowski.training.planner.network.NetworkQualifiers
import com.lukasz.witkowski.training.planner.network.networkModule
import com.lukasz.witkowski.training.planner.statistics.di.statisticsModule
import com.lukasz.witkowski.training.planner.training.di.trainingModule
import com.lukasz.witkowski.training.planner.training.editor.TrainingExerciseConfigurationViewModel
import com.lukasz.witkowski.training.planner.training.trainingSession.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.user.di.userModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val appModule = module {
    includes(networkModule, authModule, userModule, exerciseModule, trainingModule, statisticsModule)

    single<String>(NetworkQualifiers.BASE_URL) {
        BuildConfig.BASE_URL
    }

    viewModel<TrainingSessionViewModel>()
    viewModel { params ->
        TrainingExerciseConfigurationViewModel(
            service = get(),
            exerciseId = params.get(),
        )
    }
}

