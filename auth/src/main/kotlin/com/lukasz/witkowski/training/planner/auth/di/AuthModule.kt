package com.lukasz.witkowski.training.planner.auth.di

import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.domain.UserRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.DefaultAuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.DefaultUserRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.AndroidSecureTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.AndroidUserPreferencesStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.DefaultTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.SecureTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthInterceptor
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationClient
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitTokenRefreshRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitUserRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.TokenAuthenticator
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.TokenRefreshRemoteDataSource
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.UserRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import com.lukasz.witkowski.training.planner.shared.time.SystemTimeProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    includes(dispatchersModule)

    single { RetrofitAuthenticationClient.createAuthenticationApi() }

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

    single { AuthInterceptor(tokenStorage = get()) }
    single {
        TokenAuthenticator(
            tokenStorage = get(),
            tokenRefreshRemoteDataSource = get(),
        )
    }

    single {
        RetrofitAuthenticationClient(
            authInterceptor = get(),
            tokenAuthenticator = get(),
        )
    }
    single { get<RetrofitAuthenticationClient>().userApi }

    single {
        RetrofitUserRemoteDataSource(
            api = get(),
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO)),
        )
    } bind UserRemoteDataSource::class

    single { AndroidSecureTokenStorage(androidContext()) } bind SecureTokenStorage::class
    single { DefaultTokenStorage(secureTokenStorage = get()) } bind TokenStorage::class
    single { AndroidUserPreferencesStorage(androidContext()) } bind UserPreferencesStorage::class

    single {
        DefaultAuthenticationRepository(
            tokenStorage = get(),
            remoteDataSource = get(),
        )
    } bind AuthenticationRepository::class

    single {
        DefaultUserRepository(
            tokenStorage = get(),
            userRemoteDataSource = get(),
            userPreferencesStorage = get(),
        )
    } bind UserRepository::class
}
