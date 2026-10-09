package com.lukasz.witkowski.training.planner.user.di

import com.lukasz.witkowski.training.planner.network.networkModule
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import com.lukasz.witkowski.training.planner.user.infrastructure.DefaultUserRepository
import com.lukasz.witkowski.training.planner.user.infrastructure.local.AndroidUserPreferencesStorage
import com.lukasz.witkowski.training.planner.user.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.user.infrastructure.remote.RetrofitUserRemoteDataSource
import com.lukasz.witkowski.training.planner.user.infrastructure.remote.UserApi
import com.lukasz.witkowski.training.planner.user.infrastructure.remote.UserRemoteDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val userModule = module {
    includes(dispatchersModule, networkModule)

    single {
        get<Retrofit>().create(UserApi::class.java)
    }
    single {
        RetrofitUserRemoteDataSource(
            api = get(),
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO)),
            json = get(),
        )
    } bind UserRemoteDataSource::class

    single { AndroidUserPreferencesStorage(androidContext()) } bind UserPreferencesStorage::class

    single {
        DefaultUserRepository(
            userRemoteDataSource = get(),
            userPreferencesStorage = get(),
        )
    } bind UserRepository::class
}
