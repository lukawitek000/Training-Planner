package com.lukasz.witkowski.training.planner.exercise.di

import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object ExerciseModule {

    @Provides
    fun provideCategoryController(categoriesCollection: CategoriesCollection): CategoryController {
        return DefaultCategoryController(categoriesCollection)
    }

    @Provides
    fun provideCategoriesCollection(): CategoriesCollection {
        return DefaultCategoriesCollection()
    }
}
