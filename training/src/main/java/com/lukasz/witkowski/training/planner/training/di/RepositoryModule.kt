package com.lukasz.witkowski.training.planner.training.di

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.DbTrainingPlanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    abstract fun bindTrainingPlanRepository(
        dbTrainingPlanRepository: DbTrainingPlanRepository
    ): TrainingPlanRepository

}