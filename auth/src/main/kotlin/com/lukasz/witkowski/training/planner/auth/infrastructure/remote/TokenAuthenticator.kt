package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.shared.utils.fold
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val tokenRefreshRemoteDataSource: TokenRefreshRemoteDataSource,
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= RESPONSE_COUNT_LIMIT) return null

        return runBlocking {
            mutex.withLock {
                val currentAccessToken = tokenStorage.accessToken()?.token
                val requestAccessToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")

                val tokenToUse =
                    if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                        currentAccessToken
                    } else {
                        val refreshToken =
                            tokenStorage.getTokens()?.refreshToken ?: return@runBlocking null

                        tokenRefreshRemoteDataSource.refreshTokens(refreshToken).fold(
                            onSuccess = { tokens ->
                                tokenStorage.saveTokens(tokens)
                                tokens.accessToken?.token ?: tokens.refreshToken
                            },
                            onError = {
                                tokenStorage.clear()
                                return@runBlocking null
                            },
                        )
                    }

                // Retry original request with updated token
                response.request.newBuilder()
                    .header("Authorization", "Bearer $tokenToUse")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val RESPONSE_COUNT_LIMIT = 3
    }
}
