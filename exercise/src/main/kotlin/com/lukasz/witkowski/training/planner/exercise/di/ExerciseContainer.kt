package com.lukasz.witkowski.training.planner.exercise.di

import android.content.Context
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseCategoriesService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseCategoryRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDatabase
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController2
import com.lukasz.witkowski.training.planner.image.ImageStorage
import kotlinx.coroutines.Dispatchers

class ExerciseContainer(
    private val context: Context,
    private val imageStorage: ImageStorage,
) {
    private val exerciseRepository: ExerciseRepository by lazy {
        val exerciseDb = ExerciseDatabase.getInstance(context)
        DbExerciseRepository(exerciseDb.exerciseDao(), ioDispatcher = Dispatchers.IO)
    }

    val service: ExerciseService by lazy {
        ExerciseService(exerciseRepository, imageStorage)
    }

    val categoriesCollection: CategoriesCollection by lazy { DefaultCategoriesCollection() }

    val categoryController: CategoryController by lazy { DefaultCategoryController(categoriesCollection) }

    private val categoryRepository: ExerciseCategoryRepository by lazy {
        val exerciseDb = ExerciseDatabase.getInstance(context)
        DbExerciseCategoryRepository(exerciseDb.categoryDao())
    }
    private val categoryService by lazy {
        ExerciseCategoriesService(categoryRepository)
    }
    val categoryController2: CategoryController2 by lazy { DefaultCategoryController2(categoryService) }
}
