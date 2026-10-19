package com.lukasz.witkowski.training.planner.exercise.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.common.ApiErrorDto
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseConflictDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseSyncResponseDto
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.network.NetworkConfig
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails

class RetrofitExerciseRemoteDataSource(
    private val api: ExerciseApi,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json = NetworkConfig.defaultJson
) : ExerciseRemoteDataSource {

    override suspend fun getCategories(): AppResult<List<ExerciseCategory>, ExerciseFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing getCategories")
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "getCategories RemoteDataSource error")
                    exception.toExerciseFailure(json)
                }
            ) {
                api.getCategories().map { it.toExerciseCategory() }
            }
        }

    override suspend fun getExerciseChanges(
        sinceVersion: Long
    ): AppResult<ExerciseSyncResponseDto, ExerciseFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing getExerciseChanges for sinceVersion: %d", sinceVersion)
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "getExerciseChanges RemoteDataSource error")
                    exception.toExerciseFailure(json)
                }
            ) {
                api.getExerciseChanges(sinceVersion)
            }
        }

    override suspend fun getExercises(
        query: String?,
        categoryId: String?,
        page: Int?,
        limit: Int?,
        currentUserId: String?
    ): AppResult<PagedResponseDto<Exercise2>, ExerciseFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing getExercises")
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "getExercises RemoteDataSource error")
                    exception.toExerciseFailure(json)
                }
            ) {
                val pagedDto = api.getExercises(
                    query = query,
                    categoryId = categoryId,
                    page = page,
                    limit = limit
                )
                PagedResponseDto(
                    items = pagedDto.items.map { it.toExercise2(currentUserId) },
                    page = pagedDto.page,
                    limit = pagedDto.limit,
                    totalItems = pagedDto.totalItems,
                    totalPages = pagedDto.totalPages
                )
            }
        }

    override suspend fun getExerciseById(
        id: ExerciseId,
        currentUserId: String?
    ): AppResult<Exercise2, ExerciseFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing getExerciseById for id: %s", id)
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "getExerciseById RemoteDataSource error")
                    exception.toExerciseFailure(json)
                }
            ) {
                api.getExerciseById(id.toString()).toExercise2(currentUserId)
            }
        }

    override suspend fun createExercise(
        exercise: Exercise2
    ): AppResult<Exercise2, ExerciseFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing createExercise: %s", exercise.name)
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "createExercise RemoteDataSource error")
                    exception.toExerciseFailure(json)
                }
            ) {
                val request = exercise.toCreateExerciseRequestDto()
                api.createExercise(request).toExercise2()
            }
        }

    override suspend fun updateExercise(
        exerciseDetails: ExerciseDetails,
        expectedVersion: Long?,
        idempotencyKey: String?
    ): AppResult<ExerciseDto, ExerciseFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing updateExercise for id: %s", exerciseDetails.exercise.id)
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "updateExercise RemoteDataSource error")
                    exception.toExerciseFailure(json)
                }
            ) {
                val request = exerciseDetails.toUpdateExerciseRequestDto(
                    expectedVersion = expectedVersion ?: exerciseDetails.exercise.metadata.revision,
                    idempotencyKey = idempotencyKey
                )
                api.updateExercise(exerciseDetails.exercise.id.toString(), request)
            }
        }

    override suspend fun deleteExercise(
        id: ExerciseId
    ): AppResult<Unit, ExerciseFailure> =
        withContext(ioDispatcher) {
            Timber.d("RemoteDataSource executing deleteExercise for id: %s", id)
            runCatchingCancellable(
                mapError = { exception ->
                    Timber.w(exception, "deleteExercise RemoteDataSource error")
                    exception.toExerciseFailure(json)
                }
            ) {
                val response = api.deleteExercise(id.toString())
                if (!response.isSuccessful) {
                    throw HttpException(response)
                }
            }
        }
}

private fun Throwable.toExerciseFailure(json: Json): ExerciseFailure = when (this) {
    is HttpException -> {
        val errorBody = response()?.errorBody()?.string()
        val apiError =
            errorBody?.let {
                try {
                    json.decodeFromString<ApiErrorDto>(it)
                } catch (_: Exception) {
                    null
                }
            }
        when (val statusCode = code()) {
            401 -> ExerciseFailure.Unauthorized
            403 -> ExerciseFailure.Forbidden
            404 -> ExerciseFailure.ExerciseNotFound
            409 -> {
                val conflictDto = errorBody?.let {
                    try {
                        json.decodeFromString<ExerciseConflictDto>(it)
                    } catch (_: Exception) {
                        null
                    }
                }
                ExerciseFailure.Conflict(conflictDto)
            }
            else ->
                ExerciseFailure.NetworkError(
                    NetworkFailure.HttpError(
                        statusCode = statusCode,
                        message = apiError?.message ?: message(),
                        errorCode = apiError?.errorCode
                    )
                )
        }
    }

    is UnknownHostException, is ConnectException -> {
        ExerciseFailure.NetworkError(NetworkFailure.NoInternet)
    }

    is SocketTimeoutException -> {
        ExerciseFailure.NetworkError(NetworkFailure.Timeout)
    }

    is SerializationException -> {
        ExerciseFailure.NetworkError(NetworkFailure.SerializationError(message ?: ""))
    }

    is IOException -> {
        ExerciseFailure.NetworkError(NetworkFailure.Unknown)
    }

    else -> ExerciseFailure.UnknownFailure
}
