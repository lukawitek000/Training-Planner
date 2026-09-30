package com.lukasz.witkowski.training.planner.exercise.di

import com.lukasz.witkowski.training.planner.exercise.application.ExerciseCategoriesService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseCategoryRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseCategoryDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDatabase
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController2
import com.lukasz.witkowski.training.planner.image.di.imageModule
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single

val exerciseModule = module {
    includes(dispatchersModule, imageModule)

    single<ExerciseDatabase> { ExerciseDatabase.getInstance(androidContext()) }

    single<ExerciseDao> { get<ExerciseDatabase>().exerciseDao() }
    single {
        DbExerciseRepository(
            exerciseDao = get(),
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO)),
        )
    } bind ExerciseRepository::class
    single<ExerciseService>()

    single<ExerciseCategoryDao> { get<ExerciseDatabase>().categoryDao() }
    single<DbExerciseCategoryRepository>() bind ExerciseCategoryRepository::class
    single<ExerciseCategoriesService>()
    factory<DefaultCategoryController2>() bind CategoryController2::class
}
