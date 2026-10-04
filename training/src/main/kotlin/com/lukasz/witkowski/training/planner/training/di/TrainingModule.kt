package com.lukasz.witkowski.training.planner.training.di

import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.DbTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.TrainingPlanDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val trainingModule =
    module {
        single { TrainingPlanDatabase.getInstance(androidContext()) }
        single { get<TrainingPlanDatabase>().trainingPlanDao() }

        single<DbTrainingPlanRepository>() bind TrainingPlanRepository::class
        single<TrainingPlanService>()
    }
