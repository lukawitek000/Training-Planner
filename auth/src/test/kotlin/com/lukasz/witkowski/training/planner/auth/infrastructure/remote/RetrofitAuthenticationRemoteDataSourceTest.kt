package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.TestResourceReader
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
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
class RetrofitAuthenticationRemoteDataSourceTest {
    private val api = mockk<AuthenticationApi>()
    private val timeProvider = TestTimeProvider()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dataSource =
        RetrofitAuthenticationRemoteDataSource(
            api = api,
            timeProvider = timeProvider,
            ioDispatcher = testDispatcher
        )

    @Test
    fun `signIn returns success with AuthTokens and AccessToken when API call succeeds`() =
        runTest {
            val json = TestResourceReader.readJson("login_success.json")
            val authResponse = Json.decodeFromString<AuthResponseDto>(json)
            coEvery { api.login(any()) } returns authResponse

            val result = dataSource.signIn(SignInForm("test@example.com", "password"))

            assertTrue(result is AppResult.Success)
            assertEquals("access_token_123", result.value.accessToken?.token)
            assertEquals("refresh_token_xyz", result.value.refreshToken)
        }

    @Test
    fun `signIn maps duplicate email error code to UserAlreadyExists failure`() = runTest {
        val jsonError = TestResourceReader.readJson("error_user_already_exists.json")
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<AuthResponseDto>(409, responseBody))

        coEvery { api.login(any()) } throws httpException

        val result = dataSource.signIn(SignInForm("test@example.com", "password"))

        assertEquals(AppResult.Error(AuthenticationFailure.UserAlreadyExists), result)
    }

    @Test
    fun `signIn maps incorrect password error code to IncorrectPassword failure`() = runTest {
        val jsonError = TestResourceReader.readJson("error_incorrect_password.json")
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<AuthResponseDto>(401, responseBody))

        coEvery { api.login(any()) } throws httpException

        val result = dataSource.signIn(SignInForm("test@example.com", "wrongpass"))

        assertEquals(AppResult.Error(AuthenticationFailure.IncorrectPassword), result)
    }

    @Test
    fun `signIn maps user not found error code to UserNotFound failure`() = runTest {
        val jsonError = TestResourceReader.readJson("error_user_not_found.json")
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<AuthResponseDto>(404, responseBody))

        coEvery { api.login(any()) } throws httpException

        val result = dataSource.signIn(SignInForm("nobody@example.com", "password"))

        assertEquals(AppResult.Error(AuthenticationFailure.UserNotFound), result)
    }

    @Test
    fun `signIn maps UnknownHostException to NetworkError NoInternet`() = runTest {
        coEvery { api.login(any()) } throws UnknownHostException("No host")

        val result = dataSource.signIn(SignInForm("test@example.com", "password"))

        assertEquals(
            AppResult.Error(AuthenticationFailure.NetworkError(NetworkFailure.NoInternet)),
            result
        )
    }

    @Test
    fun `signIn maps SocketTimeoutException to NetworkError Timeout`() = runTest {
        coEvery { api.login(any()) } throws SocketTimeoutException("Timeout")

        val result = dataSource.signIn(SignInForm("test@example.com", "password"))

        assertEquals(
            AppResult.Error(AuthenticationFailure.NetworkError(NetworkFailure.Timeout)),
            result
        )
    }

    @Test
    fun `signIn rethrows CancellationException without swallowing it`() = runTest {
        coEvery { api.login(any()) } throws CancellationException("Job cancelled")

        assertFailsWith<CancellationException> {
            dataSource.signIn(SignInForm("test@example.com", "password"))
        }
    }

    @Test
    fun `signUp maps duplicate email error code to UserAlreadyExists failure`() = runTest {
        val jsonError = TestResourceReader.readJson("error_user_already_exists.json")
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<AuthResponseDto>(409, responseBody))

        coEvery { api.register(any()) } throws httpException

        val result = dataSource.signUp(SignUpForm("test@example.com", "username", "password"))

        assertEquals(AppResult.Error(AuthenticationFailure.UserAlreadyExists), result)
    }
}
