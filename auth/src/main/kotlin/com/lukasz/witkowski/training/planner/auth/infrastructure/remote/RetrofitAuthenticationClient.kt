package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitAuthenticationClient {
    // TODO possible a single json to use across the app
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
    }
    private val contentType = "application/json".toMediaType()

    private const val BASE_URL = "${ApiRoutes.Auth.BASE}" // TODO somehow inject the URL
    val api: AuthenticationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AuthenticationApi::class.java)
    }
}