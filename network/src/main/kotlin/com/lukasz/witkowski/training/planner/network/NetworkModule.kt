package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.time.Duration.Companion.seconds

object NetworkQualifiers {
    val AUTHENTICATED = named("authenticated")
    val UNAUTHENTICATED = named("unauthenticated")
}

object NetworkConfig {
    val defaultJson: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
        encodeDefaults = true
    }

    val contentType = "application/json".toMediaType()

    fun createUnauthenticatedOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30.seconds)
            .readTimeout(30.seconds)
            .writeTimeout(30.seconds)
            .build()
    }

    fun createAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        return createUnauthenticatedOkHttpClient().newBuilder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    fun createRetrofit(
        baseUrl: String = ApiRoutes.BASE_PATH,
        okHttpClient: OkHttpClient,
        json: Json = defaultJson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}

val networkModule = module {
    single<Json> { NetworkConfig.defaultJson }

    single(NetworkQualifiers.UNAUTHENTICATED) {
        NetworkConfig.createUnauthenticatedOkHttpClient()
    }

    single(NetworkQualifiers.UNAUTHENTICATED) {
        NetworkConfig.createRetrofit(
            baseUrl = ApiRoutes.Auth.BASE,
            okHttpClient = get(NetworkQualifiers.UNAUTHENTICATED),
            json = get(),
        )
    }

    single { AuthInterceptor(tokenStorage = get()) }
    single {
        TokenAuthenticator(
            tokenStorage = get(),
            tokenRefreshRemoteDataSource = get(),
        )
    }

    single(NetworkQualifiers.AUTHENTICATED) {
        NetworkConfig.createAuthenticatedOkHttpClient(
            authInterceptor = get(),
            tokenAuthenticator = get(),
        )
    }

    single(NetworkQualifiers.AUTHENTICATED) {
        NetworkConfig.createRetrofit(
            baseUrl = ApiRoutes.BASE_PATH,
            okHttpClient = get(NetworkQualifiers.AUTHENTICATED),
            json = get(),
        )
    }

    single<Retrofit> {
        get(NetworkQualifiers.AUTHENTICATED)
    }
}
