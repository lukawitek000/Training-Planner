package com.lukasz.witkowski.training.planner.auth.di

import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.DefaultAuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.AndroidSecureTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.DefaultTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.SecureTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationApi
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitTokenRefreshRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.presentation.auth.AuthenticationViewModel
import com.lukasz.witkowski.training.planner.network.NetworkQualifiers
import com.lukasz.witkowski.training.planner.network.TokenRefreshRemoteDataSource
import com.lukasz.witkowski.training.planner.network.TokenStorage
import com.lukasz.witkowski.training.planner.network.networkModule
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import com.lukasz.witkowski.training.planner.shared.time.SystemTimeProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val authModule = module {
    includes(dispatchersModule, networkModule)

    single {
        get<Retrofit>(NetworkQualifiers.UNAUTHENTICATED).create(AuthenticationApi::class.java)
    }

    single {
        RetrofitAuthenticationRemoteDataSource(
            api = get(),
            timeProvider = SystemTimeProvider(),
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO)),
        )
    } bind AuthenticationRemoteDataSource::class

    single {
        RetrofitTokenRefreshRemoteDataSource(
            api = get(),
            timeProvider = SystemTimeProvider(),
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO)),
        )
    } bind TokenRefreshRemoteDataSource::class

    single { AndroidSecureTokenStorage(androidContext()) } bind SecureTokenStorage::class
    single { DefaultTokenStorage(secureTokenStorage = get()) } bind TokenStorage::class

    single {
        DefaultAuthenticationRepository(
            tokenStorage = get(),
            remoteDataSource = get(),
        )
    } bind AuthenticationRepository::class

    viewModel {
        AuthenticationViewModel(
            authenticationRepository = get(),
        )
    }
}
