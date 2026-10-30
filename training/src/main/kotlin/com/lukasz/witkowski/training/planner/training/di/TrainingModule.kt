package com.lukasz.witkowski.training.planner.training.di

import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import com.lukasz.witkowski.training.planner.shared.time.SystemTimeProvider
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.DbTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.TrainingPlanDatabase
import com.lukasz.witkowski.training.planner.training.presentation.delete.DeleteTrainingPlanViewModel
import com.lukasz.witkowski.training.planner.training.presentation.details.TrainingPlanDetailsViewModel
import com.lukasz.witkowski.training.planner.training.presentation.editor.TrainingPlanEditorViewModel
import com.lukasz.witkowski.training.planner.training.presentation.list.TrainingsListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

val trainingModule =
    module {
        includes(dispatchersModule)

        single { TrainingPlanDatabase.getInstance(androidContext()) }
        single { get<TrainingPlanDatabase>().trainingPlanDao() }

        single {
            DbTrainingPlanRepository(
                trainingPlanDao = get(),
                timeProvider = SystemTimeProvider(),
                dispatcher = get(named(CoroutineDispatcherQualifiers.IO))
            )
        } bind TrainingPlanRepository::class
        single<TrainingPlanService>()

        viewModel<TrainingsListViewModel>()
        viewModel { params ->
            TrainingPlanEditorViewModel(
                trainingPlanService = get(),
                trainingPlanId = params.getOrNull(),
            )
        }
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
    }

