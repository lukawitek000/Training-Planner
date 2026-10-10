package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.shared.utils.fold
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val tokenRefreshRemoteDataSource: TokenRefreshRemoteDataSource,
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= RESPONSE_COUNT_LIMIT) {
            Timber.w("TokenAuthenticator response count limit reached for %s", response.request.url)
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentAccessToken = tokenStorage.accessToken()?.token
                val requestAccessToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")

                val tokenToUse =
                    if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                        Timber.d("TokenAuthenticator: Using updated access token from storage")
                        currentAccessToken
                    } else {
                        val refreshToken =
                            tokenStorage.getTokens()?.refreshToken ?: run {
                                Timber.w("TokenAuthenticator: No refresh token in storage")
                                return@runBlocking null
                            }

                        Timber.i("TokenAuthenticator: Refreshing tokens for 401 on %s", response.request.url)
                        tokenRefreshRemoteDataSource.refreshTokens(refreshToken).fold(
                            onSuccess = { tokens ->
                                Timber.i("TokenAuthenticator: Token refresh succeeded")
                                tokenStorage.saveTokens(tokens)
                                tokens.accessToken?.token ?: tokens.refreshToken
                            },
                            onError = { failure ->
                                Timber.w("TokenAuthenticator: Token refresh failed: %s - clearing token storage", failure)
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
