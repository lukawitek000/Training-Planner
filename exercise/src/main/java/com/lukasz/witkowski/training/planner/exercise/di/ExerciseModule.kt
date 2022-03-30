package com.lukasz.witkowski.training.planner.exercise.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.exercise.application.CategoryService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ExerciseModule {

    @Singleton
    @Provides
    fun provideExerciseService(exerciseRepository: ExerciseRepository): ExerciseService {
        return ExerciseService(exerciseRepository)
    }

    @Singleton
    @Provides
    fun provideCategoryService(): CategoryService {
        return CategoryService()
    }

    @Singleton
    @Provides
    fun provideDbExerciseRepository(exerciseDao: ExerciseDao): DbExerciseRepository {
        return DbExerciseRepository(exerciseDao)
    }

    @Singleton
    @Provides
    fun provideExerciseDatabase(@ApplicationContext context: Context): ExerciseDatabase {
        return Room.databaseBuilder(
            context,
            ExerciseDatabase::class.java,
            "Exercise Database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideExerciseDao(exerciseDatabase: ExerciseDatabase) = exerciseDatabase.exerciseDao()
}
