package com.lukasz.witkowski.training.planner.exercise.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDatabase
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.ImageStorageFactory
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
    fun provideExerciseService(exerciseRepository: ExerciseRepository, imageStorage: ImageStorage): ExerciseService {
        return ExerciseService(exerciseRepository, imageStorage)
    }

    @Provides
    fun provideCategoryController(categoriesCollection: CategoriesCollection): CategoryController {
        return DefaultCategoryController(categoriesCollection)
    }

    @Provides
    fun provideCategoriesCollection(): CategoriesCollection {
        return DefaultCategoriesCollection()
    }

    @Singleton
    @Provides
    fun provideDbExerciseRepository(exerciseDao: ExerciseDao): DbExerciseRepository {
        return DbExerciseRepository(exerciseDao)
    }

    @Singleton
    @Provides
    fun provideDefaultImageStorage(@ApplicationContext context: Context): ImageStorage {
        return ImageStorageFactory.create(context, "exercise_images")
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
