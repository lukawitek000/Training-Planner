package com.lukasz.witkowski.training.planner.auth.di

import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.DefaultAuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.AndroidKeystoreTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationClient
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import com.lukasz.witkowski.training.planner.shared.time.SystemTimeProvider
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val authModule = module {
    includes(dispatchersModule)

    single { RetrofitAuthenticationClient.api }
    single {
        RetrofitAuthenticationRemoteDataSource(
            api = get(),
            timeProvider = SystemTimeProvider(),
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO)),
        )
    } bind AuthenticationRemoteDataSource::class
    single<AndroidKeystoreTokenStorage>() bind TokenStorage::class
    single {
        DefaultAuthenticationRepository(
            tokenStorage = get(),
            remoteDataSource = get(),
        )
    } bind AuthenticationRepository::class

}