package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.User
import com.lukasz.witkowski.training.planner.auth.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitUserRemoteDataSourceTest {

    private val api = mockk<UserApi>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dataSource = RetrofitUserRemoteDataSource(
        api = api,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getUserProfile returns success with User when API call succeeds`() = runTest {
        val userDto = UserDto("user-123", "test@example.com", "TestUser")
        coEvery { api.me("Bearer test-token") } returns userDto

        val result = dataSource.getUserProfile("test-token")

        assertTrue(result is AppResult.Success)
        assertEquals(User("user-123", "test@example.com", "TestUser"), result.value)
    }

    @Test
    fun `getUserProfile maps 401 error to Unauthorized failure`() = runTest {
        val jsonError = """{"statusCode":401,"message":"Unauthorized"}"""
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<UserDto>(401, responseBody))

        coEvery { api.me(any()) } throws httpException

        val result = dataSource.getUserProfile("invalid-token")

        assertEquals(AppResult.Error(UserFailure.Unauthorized), result)
    }

    @Test
    fun `getUserProfile maps user not found error code to UserNotFound failure`() = runTest {
        val jsonError = """{"statusCode":404,"message":"User not found","errorCode":"USER_NOT_FOUND"}"""
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<UserDto>(404, responseBody))

        coEvery { api.me(any()) } throws httpException

        val result = dataSource.getUserProfile("test-token")

        assertEquals(AppResult.Error(UserFailure.UserNotFound), result)
    }

    @Test
    fun `getUserProfile maps UnknownHostException to NetworkError NoInternet`() = runTest {
        coEvery { api.me(any()) } throws UnknownHostException("No host")

        val result = dataSource.getUserProfile("test-token")

        assertEquals(AppResult.Error(UserFailure.NetworkError(NetworkFailure.NoInternet)), result)
    }

    @Test
    fun `getUserProfile maps SocketTimeoutException to NetworkError Timeout`() = runTest {
        coEvery { api.me(any()) } throws SocketTimeoutException("Timeout")

        val result = dataSource.getUserProfile("test-token")

        assertEquals(AppResult.Error(UserFailure.NetworkError(NetworkFailure.Timeout)), result)
    }

    @Test
    fun `getUserProfile rethrows CancellationException without swallowing it`() = runTest {
        coEvery { api.me(any()) } throws CancellationException("Job cancelled")

        assertFailsWith<CancellationException> {
            dataSource.getUserProfile("test-token")
        }
    }
}
