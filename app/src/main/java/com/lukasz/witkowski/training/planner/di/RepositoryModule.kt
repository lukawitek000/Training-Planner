package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
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
    fun provideTrainingRepository(trainingDao: TrainingDao): TrainingRepository {
        return TrainingRepository(trainingDao = trainingDao)
    }

    @Singleton
    @Provides
    fun provideStatisticsRepository(statisticsDao: StatisticsDao): StatisticsRepository {
        return StatisticsRepository(statisticsDao = statisticsDao)
    }
}
