package com.lukasz.witkowski.training.planner.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

object NetworkQualifiers {
    val AUTHENTICATED = named("authenticated")
    val UNAUTHENTICATED = named("unauthenticated")
    val BASE_URL = named("baseUrl")
}

object NetworkConfig {
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/"

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
        baseUrl: String = DEFAULT_BASE_URL,
        okHttpClient: OkHttpClient,
        json: Json = defaultJson,
    ): Retrofit {
        Timber.i("Creating Retrofit instance with baseUrl: %s", baseUrl)
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
            baseUrl = getOrNull(NetworkQualifiers.BASE_URL) ?: NetworkConfig.DEFAULT_BASE_URL,
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
            baseUrl = getOrNull(NetworkQualifiers.BASE_URL) ?: NetworkConfig.DEFAULT_BASE_URL,
            okHttpClient = get(NetworkQualifiers.AUTHENTICATED),
            json = get(),
        )
    }

    single<Retrofit> {
        get(NetworkQualifiers.AUTHENTICATED)
    }
}
