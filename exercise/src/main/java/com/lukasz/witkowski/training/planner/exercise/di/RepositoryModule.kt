package com.lukasz.witkowski.training.planner.exercise.di

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ImageRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    abstract fun bindExerciseRepository(
        dbExerciseRepository: DbExerciseRepository
    ): ExerciseRepository

    @Binds
    abstract fun bindImageRepository(
        internalStorageImageRepository: InternalStorageImageRepository
    ): ImageRepository
}