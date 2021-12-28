package com.lukasz.witkowski.training.wearable.di

import com.lukasz.witkowski.shared.db.ExerciseDao
import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.training.wearable.repo.CurrentTrainingRepository
import com.lukasz.witkowski.training.wearable.repo.TrainingRepository
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