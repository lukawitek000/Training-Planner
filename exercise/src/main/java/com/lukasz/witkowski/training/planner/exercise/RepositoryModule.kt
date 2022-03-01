package com.lukasz.witkowski.training.planner.exercise

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton

@Module
@InstallIn(Singleton::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindExerciseRepository(
        dbExerciseRepository: DbExerciseRepository
    ): ExerciseRepository
}