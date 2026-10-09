package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.time.Duration.Companion.seconds

class RetrofitAuthenticationClient(
    private val authInterceptor: AuthInterceptor,
    private val tokenAuthenticator: TokenAuthenticator,
) {
    val defaultJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
    }
    private val contentType = "application/json".toMediaType()
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(30.seconds)
        .readTimeout(30.seconds)
        .writeTimeout(30.seconds)
        .build()

    fun createAuthenticationApi(
        baseUrl: String = ApiRoutes.Auth.BASE,
    ): AuthenticationApi {
        return buildRetrofit(baseUrl).create(AuthenticationApi::class.java)
    }

    fun createUserApi(
        baseUrl: String = ApiRoutes.Auth.BASE,
    ): UserApi {
        return buildRetrofit(baseUrl).create(UserApi::class.java)
    }

    val api: AuthenticationApi by lazy {
        createAuthenticationApi()
    }

    val userApi: UserApi by lazy {
        createUserApi()
    }

    private fun buildRetrofit(baseUrl: String) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(defaultJson.asConverterFactory(contentType))
        .build()

    companion object {
        fun createAuthenticationApi(
            baseUrl: String = ApiRoutes.Auth.BASE,
            json: Json = Json {
                ignoreUnknownKeys = true
                explicitNulls = true
            },
        ): AuthenticationApi {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(AuthenticationApi::class.java)
        }
    }
}
