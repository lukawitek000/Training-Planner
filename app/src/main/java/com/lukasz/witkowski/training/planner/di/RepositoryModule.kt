package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.shared.db.ExerciseDao
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
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

}