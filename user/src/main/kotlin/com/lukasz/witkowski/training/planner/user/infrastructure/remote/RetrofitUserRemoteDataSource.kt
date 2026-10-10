package com.lukasz.witkowski.training.planner.user.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import com.lukasz.witkowski.training.planner.dto.common.ApiErrorDto
import com.lukasz.witkowski.training.planner.network.NetworkConfig
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
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

class RetrofitUserRemoteDataSource(
    private val api: UserApi,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json = NetworkConfig.defaultJson,
) : UserRemoteDataSource {

    override suspend fun getUserProfile(): AppResult<User, UserFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing getUserProfile")
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "getUserProfile RemoteDataSource error")
                    exception.toUserFailure(json)
                },
            ) {
                api.me().toUser()
            }
        }
}

internal fun UserDto.toUser(): User = User(id = id, email = email, username = username)

private fun Throwable.toUserFailure(json: Json): UserFailure {
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
                "USER_NOT_FOUND" -> UserFailure.UserNotFound
                else -> {
                    when (val statusCode = code()) {
                        401 -> UserFailure.Unauthorized
                        404 -> UserFailure.UserNotFound
                        else -> UserFailure.NetworkError(
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
            UserFailure.NetworkError(NetworkFailure.NoInternet)
        }

        is SocketTimeoutException -> {
            UserFailure.NetworkError(NetworkFailure.Timeout)
        }

        is SerializationException -> {
            UserFailure.NetworkError(NetworkFailure.SerializationError(message))
        }

        is IOException -> {
            UserFailure.NetworkError(NetworkFailure.Unknown)
        }

        else -> UserFailure.UnknownFailure
    }
}
