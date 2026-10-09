package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitTokenRefreshRemoteDataSourceTest {

    private val api = mockk<AuthenticationApi>()
    private val timeProvider = TestTimeProvider()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dataSource = RetrofitTokenRefreshRemoteDataSource(
        api = api,
        timeProvider = timeProvider,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `refreshTokens returns success with AuthTokens when API call succeeds`() = runTest {
        val tokenResponse = TokenResponseDto("new-access-123", "new-refresh-123", 3600000)
        coEvery { api.refresh(any()) } returns tokenResponse

        val result = dataSource.refreshTokens("valid-refresh-token")

        assertTrue(result is AppResult.Success)
        assertEquals("new-access-123", result.value.accessToken?.token)
        assertEquals("new-refresh-123", result.value.refreshToken)
    }

    @Test
    fun `refreshTokens maps invalid refresh token error code to InvalidRefreshToken failure`() = runTest {
        val jsonError = """{"statusCode":401,"message":"Invalid or expired refresh token","errorCode":"INVALID_REFRESH_TOKEN"}"""
        val responseBody = jsonError.toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<TokenResponseDto>(401, responseBody))

        coEvery { api.refresh(any()) } throws httpException

        val result = dataSource.refreshTokens("invalid-refresh-token")

        assertEquals(AppResult.Error(AuthenticationFailure.InvalidRefreshToken), result)
    }
}
