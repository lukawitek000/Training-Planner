package com.lukasz.witkowski.training.planner.exercise.di

import com.lukasz.witkowski.training.planner.exercise.application.ExerciseCategoriesService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepositoryConfig
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DefaultExerciseCategoryRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DefaultExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseCategoryDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDatabase
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.ExerciseCategoryLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.ExerciseLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.RoomExerciseCategoryLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.RoomExerciseLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseApi
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.RetrofitExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController2
import com.lukasz.witkowski.training.planner.image.di.imageModule
import com.lukasz.witkowski.training.planner.network.networkModule
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import retrofit2.Retrofit
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMutationScheduler
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseMutationDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.sync.DefaultExerciseSyncRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.sync.WorkManagerExerciseMutationScheduler

val exerciseModule =
    module {
        includes(dispatchersModule, imageModule, networkModule)

        single { ExerciseRepositoryConfig() }

        single {
            get<Retrofit>().create(ExerciseApi::class.java)
        }
        single {
            RetrofitExerciseRemoteDataSource(
                api = get(),
                ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO)),
                json = get()
            )
        } bind ExerciseRemoteDataSource::class

        single<ExerciseDatabase> { ExerciseDatabase.getInstance(androidContext()) }

        single<ExerciseDao> { get<ExerciseDatabase>().exerciseDao() }
        single<ExerciseMutationDao> { get<ExerciseDatabase>().mutationDao() }

        single {
            RoomExerciseLocalDataSource(
                exerciseDao = get(),
                mutationDao = get()
            )
        } bind ExerciseLocalDataSource::class

        single<ExerciseMutationScheduler> { WorkManagerExerciseMutationScheduler(androidContext()) }

        single {
            DefaultExerciseRepository(
                localDataSource = get(),
                remoteDataSource = get(),
                mutationScheduler = get(),
                config = get(),
                ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO))
            )
        } bind ExerciseRepository::class

        single {
            DefaultExerciseSyncRepository(
                exerciseDao = get(),
                mutationDao = get(),
                remoteDataSource = get(),
                json = get(),
                ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO))
            )
        } bind ExerciseSyncRepository::class
        single<ExerciseService>()

        single<ExerciseCategoryDao> { get<ExerciseDatabase>().categoryDao() }
        single {
            RoomExerciseCategoryLocalDataSource(categoryDao = get())
        } bind ExerciseCategoryLocalDataSource::class

        single {
            DefaultExerciseCategoryRepository(
                localDataSource = get(),
                remoteDataSource = get(),
                ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO))
            )
        } bind ExerciseCategoryRepository::class

        single<ExerciseCategoriesService>()
        factory<DefaultCategoryController2>() bind CategoryController2::class
    }
