package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenAuthenticatorTest {

    private val mockTokenStorage = mockk<TokenStorage>()
    private val mockTokenRefreshRemoteDataSource = mockk<TokenRefreshRemoteDataSource>()
    private val timeProvider = TestTimeProvider()

    private val authenticator = TokenAuthenticator(
        tokenStorage = mockTokenStorage,
        tokenRefreshRemoteDataSource = mockTokenRefreshRemoteDataSource,
    )

    private fun createResponse(request: Request, responseCode: Int = 401, priorResponse: Response? = null): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(responseCode)
            .message("Unauthorized")
            .priorResponse(priorResponse)
            .build()
    }

    @Test
    fun `authenticate returns null when response count limit is reached`() {
        val request = Request.Builder().url("https://example.com/api/me").build()
        val response1 = createResponse(request)
        val response2 = createResponse(request, priorResponse = response1)
        val response3 = createResponse(request, priorResponse = response2)

        val result = authenticator.authenticate(null, response3)

        assertNull(result)
    }

    @Test
    fun `authenticate uses newer access token if token storage was already updated`() {
        val failedRequest = Request.Builder()
            .url("https://example.com/api/me")
            .header("Authorization", "Bearer old-token")
            .build()
        val response = createResponse(failedRequest)

        val newAccessToken = AccessToken("new-token", timeProvider.currentInstant())
        every { mockTokenStorage.accessToken() } returns newAccessToken

        val result = authenticator.authenticate(null, response)

        assertEquals("Bearer new-token", result?.header("Authorization"))
    }

    @Test
    fun `authenticate refreshes token when refresh token is available and API returns success`() {
        val failedRequest = Request.Builder()
            .url("https://example.com/api/me")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = createResponse(failedRequest)

        val currentTokens = AuthTokens(
            accessToken = AccessToken("expired-token", timeProvider.currentInstant()),
            refreshToken = "valid-refresh-token",
        )
        val refreshedTokens = AuthTokens(
            accessToken = AccessToken("refreshed-access-token", timeProvider.currentInstant()),
            refreshToken = "new-refresh-token",
        )

        every { mockTokenStorage.accessToken() } returns currentTokens.accessToken
        coEvery { mockTokenStorage.getTokens() } returns currentTokens
        coEvery { mockTokenRefreshRemoteDataSource.refreshTokens("valid-refresh-token") } returns AppResult.Success(refreshedTokens)
        coEvery { mockTokenStorage.saveTokens(refreshedTokens) } returns Unit

        val result = authenticator.authenticate(null, response)

        assertEquals("Bearer refreshed-access-token", result?.header("Authorization"))
        coVerify { mockTokenStorage.saveTokens(refreshedTokens) }
    }

    @Test
    fun `authenticate clears token storage and returns null when refreshTokens fails`() {
        val failedRequest = Request.Builder()
            .url("https://example.com/api/me")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = createResponse(failedRequest)

        val currentTokens = AuthTokens(
            accessToken = AccessToken("expired-token", timeProvider.currentInstant()),
            refreshToken = "invalid-refresh-token",
        )

        every { mockTokenStorage.accessToken() } returns currentTokens.accessToken
        coEvery { mockTokenStorage.getTokens() } returns currentTokens
        coEvery { mockTokenRefreshRemoteDataSource.refreshTokens("invalid-refresh-token") } returns AppResult.Error(TokenRefreshFailure.InvalidRefreshToken)
        coEvery { mockTokenStorage.clear() } returns Unit

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        coVerify { mockTokenStorage.clear() }
    }

    @Test
    fun `authenticate returns null when no refresh token is stored`() {
        val failedRequest = Request.Builder()
            .url("https://example.com/api/me")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = createResponse(failedRequest)

        every { mockTokenStorage.accessToken() } returns null
        coEvery { mockTokenStorage.getTokens() } returns null

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }
}
