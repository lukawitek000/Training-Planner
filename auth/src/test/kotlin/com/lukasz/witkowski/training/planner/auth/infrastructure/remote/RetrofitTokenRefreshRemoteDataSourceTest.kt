package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.TestResourceReader
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.network.TokenRefreshFailure
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitTokenRefreshRemoteDataSourceTest {
    private val api = mockk<AuthenticationApi>()
    private val timeProvider = TestTimeProvider()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dataSource =
        RetrofitTokenRefreshRemoteDataSource(
            api = api,
            timeProvider = timeProvider,
            ioDispatcher = testDispatcher
        )

    @Test
    fun `refreshTokens returns success with AuthTokens when API call succeeds`() = runTest {
        val json = TestResourceReader.readJson("refresh_success.json")
        val tokenResponse = Json.decodeFromString<TokenResponseDto>(json)
        coEvery { api.refresh(any()) } returns tokenResponse

        val result = dataSource.refreshTokens("valid-refresh-token")

        assertTrue(result is AppResult.Success)
        assertEquals("new_access_token_789", result.value.accessToken?.token)
        assertEquals("new_refresh_token_999", result.value.refreshToken)
    }

    @Test
    fun `refreshTokens maps invalid refresh token error code to InvalidRefreshToken failure`() =
        runTest {
            val jsonError = TestResourceReader.readJson("error_invalid_refresh_token.json")
            val responseBody = jsonError.toResponseBody("application/json".toMediaType())
            val httpException = HttpException(Response.error<TokenResponseDto>(401, responseBody))

            coEvery { api.refresh(any()) } throws httpException

            val result = dataSource.refreshTokens("invalid-refresh-token")

            assertEquals(AppResult.Error(TokenRefreshFailure.InvalidRefreshToken), result)
        }
}
