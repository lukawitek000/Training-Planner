package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitAuthenticationClient {
    val defaultJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
    }
    private val contentType = "application/json".toMediaType()

    fun createAuthenticationApi(
        baseUrl: String = ApiRoutes.Auth.BASE,
        json: Json = defaultJson,
    ): AuthenticationApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AuthenticationApi::class.java)
    }

    val api: AuthenticationApi by lazy {
        createAuthenticationApi()
    }
}
