package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.training.planner.exercise.application.CategoryService
import com.lukasz.witkowski.training.planner.exercise.exercisesList.CategoryController
import com.lukasz.witkowski.training.planner.exercise.exercisesList.DefaultCategoryController
import com.lukasz.witkowski.training.planner.repository.StatisticsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
object ExerciseModule {
    @Provides
    fun provideCategoryController(categoryService: CategoryService): CategoryController {
        return DefaultCategoryController(categoryService)
    }
}