package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.shared.db.ExerciseDao
import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
import com.lukasz.witkowski.training.planner.repository.SyncDataRepository
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
    fun provideExerciseRepository(
        exerciseDao: ExerciseDao
    ): ExerciseRepository {
        return ExerciseRepository(exerciseDao = exerciseDao)
    }

    @Singleton
    @Provides
    fun provideTrainingRepository(trainingDao: TrainingDao): TrainingRepository {
        return TrainingRepository(trainingDao = trainingDao)
    }

    @Singleton
    @Provides
    fun provideSyncDataRepository(trainingDao: TrainingDao): SyncDataRepository {
        return SyncDataRepository(trainingDao = trainingDao)
    }
}
