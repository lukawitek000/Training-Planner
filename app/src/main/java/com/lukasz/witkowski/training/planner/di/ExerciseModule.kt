package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.exercise.application.CategoryService
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ExerciseModule {
    @Provides
    fun provideCategoryController(categoryService: CategoryService): CategoryController {
        return DefaultCategoryController(categoryService)
    }
}