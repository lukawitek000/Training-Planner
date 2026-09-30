package com.lukasz.witkowski.training.planner.training.di

import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.infrastructure.db.DbTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.TrainingPlanDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.WearableTrainingPlanSender
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val trainingModule = module {
    single { TrainingPlanDatabase.getInstance(androidContext()) }
    single { get<TrainingPlanDatabase>().trainingPlanDao() }

    single<DbTrainingPlanRepository>() bind TrainingPlanRepository::class
    single<WearableTrainingPlanSender>() bind TrainingPlanSender::class
    single<TrainingPlanService>()
}