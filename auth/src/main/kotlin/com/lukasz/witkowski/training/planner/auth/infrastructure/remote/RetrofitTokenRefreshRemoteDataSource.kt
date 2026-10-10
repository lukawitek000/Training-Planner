package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.auth.RefreshTokenRequestDto
import com.lukasz.witkowski.training.planner.dto.common.ApiErrorDto
import com.lukasz.witkowski.training.planner.network.AuthTokens
import com.lukasz.witkowski.training.planner.network.TokenRefreshFailure
import com.lukasz.witkowski.training.planner.network.TokenRefreshRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.time.TimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RetrofitTokenRefreshRemoteDataSource(
    private val api: AuthenticationApi,
    private val timeProvider: TimeProvider,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json = defaultJson,
) : TokenRefreshRemoteDataSource {

    override suspend fun refreshTokens(refreshToken: String): AppResult<AuthTokens, TokenRefreshFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing refreshTokens")
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "refreshTokens RemoteDataSource failed")
                    exception.toTokenRefreshFailure(json)
                },
            ) {
                api.refresh(RefreshTokenRequestDto(refreshToken))
                    .toAuthTokens(timeProvider.currentInstant())
            }
        }

    companion object {
        private val defaultJson = Json {
            ignoreUnknownKeys = true
            explicitNulls = true
        }
    }
}

private fun Throwable.toTokenRefreshFailure(json: Json): TokenRefreshFailure {
    return when (this) {
        is HttpException -> {
            val errorBody = response()?.errorBody()?.string()
            val apiError = errorBody?.let {
                try {
                    json.decodeFromString<ApiErrorDto>(it)
                } catch (_: Exception) {
                    null
                }
            }
            when (apiError?.errorCode) {
                "INVALID_REFRESH_TOKEN" -> TokenRefreshFailure.InvalidRefreshToken
                "USER_NOT_FOUND" -> TokenRefreshFailure.UserNotFound
                else -> {
                    when (val statusCode = code()) {
                        401 -> TokenRefreshFailure.InvalidRefreshToken
                        404 -> TokenRefreshFailure.UserNotFound
                        else -> TokenRefreshFailure.NetworkError(
                            NetworkFailure.HttpError(
                                statusCode = statusCode,
                                message = apiError?.message ?: message(),
                                errorCode = apiError?.errorCode,
                            ),
                        )
                    }
                }
            }
        }

        is UnknownHostException, is ConnectException -> {
            TokenRefreshFailure.NetworkError(NetworkFailure.NoInternet)
        }

        is SocketTimeoutException -> {
            TokenRefreshFailure.NetworkError(NetworkFailure.Timeout)
        }

        is SerializationException -> {
            TokenRefreshFailure.NetworkError(NetworkFailure.SerializationError(message))
        }

        is IOException -> {
            TokenRefreshFailure.NetworkError(NetworkFailure.Unknown)
        }

        else -> TokenRefreshFailure.UnknownFailure
    }
}
