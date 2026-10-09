package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
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
class RetrofitAuthenticationRemoteDataSourceTest {

    private val api = mockk<AuthenticationApi>()
    private val timeProvider = TestTimeProvider()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dataSource =
        RetrofitAuthenticationRemoteDataSource(
            api = api,
            timeProvider = timeProvider,
            ioDispatcher = testDispatcher,
        )

    @Test
    fun `signIn returns success with AuthTokens and AccessToken when API call succeeds`() =
        runTest {
            val tokenResponse = TokenResponseDto("access-123", "refresh-123", 3600000)
            val authResponse = AuthResponseDto(UserDto("user-1", "test@example.com", "TestUser"), tokenResponse)
            coEvery { api.login(any()) } returns authResponse

            val result = dataSource.signIn(SignInForm("test@example.com", "password"))

            assertTrue(result is AppResult.Success)
            assertEquals("access-123", result.value.accessToken?.token)
            assertEquals("refresh-123", result.value.refreshToken)
        }

    @Test
    fun `signIn maps duplicate email error code to UserAlreadyExists failure`() =
        runTest {
            val jsonError = """{"statusCode":409,"message":"User already exists","errorCode":"USER_ALREADY_EXISTS"}"""
            val responseBody = jsonError.toResponseBody("application/json".toMediaType())
            val httpException = HttpException(Response.error<AuthResponseDto>(409, responseBody))

            coEvery { api.login(any()) } throws httpException

            val result = dataSource.signIn(SignInForm("test@example.com", "password"))

            assertEquals(AppResult.Error(AuthenticationFailure.UserAlreadyExists), result)
        }

    @Test
    fun `signIn maps incorrect password error code to IncorrectPassword failure`() =
        runTest {
            val jsonError = """{"statusCode":401,"message":"Incorrect password","errorCode":"INCORRECT_PASSWORD"}"""
            val responseBody = jsonError.toResponseBody("application/json".toMediaType())
            val httpException = HttpException(Response.error<AuthResponseDto>(401, responseBody))

            coEvery { api.login(any()) } throws httpException

            val result = dataSource.signIn(SignInForm("test@example.com", "wrongpass"))

            assertEquals(AppResult.Error(AuthenticationFailure.IncorrectPassword), result)
        }

    @Test
    fun `signIn maps user not found error code to UserNotFound failure`() =
        runTest {
            val jsonError = """{"statusCode":404,"message":"User not found","errorCode":"USER_NOT_FOUND"}"""
            val responseBody = jsonError.toResponseBody("application/json".toMediaType())
            val httpException = HttpException(Response.error<AuthResponseDto>(404, responseBody))

            coEvery { api.login(any()) } throws httpException

            val result = dataSource.signIn(SignInForm("nobody@example.com", "password"))

            assertEquals(AppResult.Error(AuthenticationFailure.UserNotFound), result)
        }

    @Test
    fun `signIn maps UnknownHostException to NetworkError NoInternet`() =
        runTest {
            coEvery { api.login(any()) } throws UnknownHostException("No host")

            val result = dataSource.signIn(SignInForm("test@example.com", "password"))

            assertEquals(AppResult.Error(AuthenticationFailure.NetworkError(NetworkFailure.NoInternet)), result)
        }

    @Test
    fun `signIn maps SocketTimeoutException to NetworkError Timeout`() =
        runTest {
            coEvery { api.login(any()) } throws SocketTimeoutException("Timeout")

            val result = dataSource.signIn(SignInForm("test@example.com", "password"))

            assertEquals(AppResult.Error(AuthenticationFailure.NetworkError(NetworkFailure.Timeout)), result)
        }

    @Test
    fun `signIn rethrows CancellationException without swallowing it`() =
        runTest {
            coEvery { api.login(any()) } throws CancellationException("Job cancelled")

            assertFailsWith<CancellationException> {
                dataSource.signIn(SignInForm("test@example.com", "password"))
            }
        }

    @Test
    fun `signUp maps duplicate email error code to UserAlreadyExists failure`() =
        runTest {
            val jsonError = """{"statusCode":409,"message":"User already exists","errorCode":"USER_ALREADY_EXISTS"}"""
            val responseBody = jsonError.toResponseBody("application/json".toMediaType())
            val httpException = HttpException(Response.error<AuthResponseDto>(409, responseBody))

            coEvery { api.register(any()) } throws httpException

            val result = dataSource.signUp(SignUpForm("test@example.com", "username", "password"))

            assertEquals(AppResult.Error(AuthenticationFailure.UserAlreadyExists), result)
        }
}
