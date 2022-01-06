package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.repo.CurrentTrainingRepository
import com.lukasz.witkowski.training.planner.repo.TrainingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideTrainingRepository(): TrainingRepository {
        return TrainingRepository()
    }

    @Singleton
    @Provides
    fun provideCurrentTrainingRepository(): CurrentTrainingRepository {
        return CurrentTrainingRepository()
    }
}