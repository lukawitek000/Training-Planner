package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.shared.db.TrainingDao
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
    fun provideTrainingRepository(trainingDao: TrainingDao, statisticsDao: StatisticsDao): TrainingRepository {
        return TrainingRepository(trainingDao, statisticsDao)
    }
}
