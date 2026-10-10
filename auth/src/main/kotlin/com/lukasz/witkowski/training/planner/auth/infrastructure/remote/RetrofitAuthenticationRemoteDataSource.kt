package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.dto.common.ApiErrorDto
import com.lukasz.witkowski.training.planner.network.AuthTokens
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

class RetrofitAuthenticationRemoteDataSource(
    private val api: AuthenticationApi,
    private val timeProvider: TimeProvider,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json = defaultJson,
) : AuthenticationRemoteDataSource {

    override suspend fun signIn(signInForm: SignInForm): AppResult<AuthTokens, AuthenticationFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing signIn for email: %s", signInForm.email)
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "signIn RemoteDataSource error for email: %s", signInForm.email)
                    exception.toAuthenticationFailure(json)
                },
            ) {
                api.login(signInForm.toLoginRequestDto())
                    .toAuthTokens(timeProvider.currentInstant())
            }
        }

    override suspend fun signUp(signUpForm: SignUpForm): AppResult<AuthTokens, AuthenticationFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing signUp for email: %s", signUpForm.email)
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "signUp RemoteDataSource error for email: %s", signUpForm.email)
                    exception.toAuthenticationFailure(json)
                },
            ) {
                api.register(signUpForm.toRegisterRequestDto())
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

private fun Throwable.toAuthenticationFailure(json: Json): AuthenticationFailure {
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
                "USER_ALREADY_EXISTS" -> AuthenticationFailure.UserAlreadyExists
                "USER_NOT_FOUND" -> AuthenticationFailure.UserNotFound
                "INCORRECT_PASSWORD" -> AuthenticationFailure.IncorrectPassword
                "INVALID_REFRESH_TOKEN" -> AuthenticationFailure.InvalidRefreshToken
                else -> {
                    when (val statusCode = code()) {
                        409 -> AuthenticationFailure.UserAlreadyExists
                        404 -> AuthenticationFailure.UserNotFound
                        401 -> AuthenticationFailure.IncorrectPassword
                        else -> AuthenticationFailure.NetworkError(
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
            AuthenticationFailure.NetworkError(NetworkFailure.NoInternet)
        }

        is SocketTimeoutException -> {
            AuthenticationFailure.NetworkError(NetworkFailure.Timeout)
        }

        is SerializationException -> {
            AuthenticationFailure.NetworkError(NetworkFailure.SerializationError(message))
        }

        is IOException -> {
            AuthenticationFailure.NetworkError(NetworkFailure.Unknown)
        }

        else -> AuthenticationFailure.UnknownFailure
    }
}
