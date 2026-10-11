package com.lukasz.witkowski.training.planner.user.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.TestResourceReader
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import io.mockk.coEvery
import io.mockk.mockk
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("ktlint:standard:max-line-length")
class RetrofitUserRemoteDataSourceTest {
    private val api = mockk<UserApi>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dataSource =
        RetrofitUserRemoteDataSource(
            api = api,
            ioDispatcher = testDispatcher
        )

    @Test
    fun `getUserProfile returns success with User when API call succeeds`() = runTest {
        val json = TestResourceReader.readJson("user_me_success.json")
        val userDto = Json.decodeFromString<UserDto>(json)
        coEvery { api.me() } returns userDto

        val result = dataSource.getUserProfile()

        assertTrue(result is AppResult.Success)
        assertEquals(User("user-123", "test@example.com", "TestUser"), result.value)
    }

    @Test
    fun `getUserProfile maps 401 error to Unauthorized failure`() = runTest {
        val jsonError = TestResourceReader.readJson("error_unauthorized.json")
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<UserDto>(401, responseBody))

        coEvery { api.me() } throws httpException

        val result = dataSource.getUserProfile()

        assertEquals(AppResult.Error(UserFailure.Unauthorized), result)
    }

    @Test
    fun `getUserProfile maps user not found error code to UserNotFound failure`() = runTest {
        val jsonError = TestResourceReader.readJson("error_user_not_found.json")
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<UserDto>(404, responseBody))

        coEvery { api.me() } throws httpException

        val result = dataSource.getUserProfile()

        assertEquals(AppResult.Error(UserFailure.UserNotFound), result)
    }

    @Test
    fun `getUserProfile maps UnknownHostException to NetworkError NoInternet`() = runTest {
        coEvery { api.me() } throws UnknownHostException("No host")

        val result = dataSource.getUserProfile()

        assertEquals(
            AppResult.Error(UserFailure.NetworkError(NetworkFailure.NoInternet)),
            result
        )
    }

    @Test
    fun `getUserProfile maps SocketTimeoutException to NetworkError Timeout`() = runTest {
        coEvery { api.me() } throws SocketTimeoutException("Timeout")

        val result = dataSource.getUserProfile()

        assertEquals(
            AppResult.Error(UserFailure.NetworkError(NetworkFailure.Timeout)),
            result
        )
    }

    @Test
    fun `getUserProfile rethrows CancellationException without swallowing it`() = runTest {
        coEvery { api.me() } throws CancellationException("Job cancelled")

        assertFailsWith<CancellationException> {
            dataSource.getUserProfile()
        }
    }
}
