package com.lukasz.witkowski.training.planner.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenStorage.accessToken()?.token
        val authenticatedRequest = originalRequest.newBuilder()
            .apply {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
            }
            .build()
        return chain.proceed(authenticatedRequest)
    }
}
