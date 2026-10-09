package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthInterceptorTest {

    private val mockTokenStorage = mockk<TokenStorage>()
    private val timeProvider = TestTimeProvider()
    private val interceptor = AuthInterceptor(mockTokenStorage)

    @Test
    fun `intercept adds Authorization Bearer header to request`() {
        val accessToken = AccessToken("my-access-token", timeProvider.currentInstant())
        every { mockTokenStorage.accessToken() } returns accessToken

        val dummyRequest = Request.Builder()
            .url("https://example.com/api/test")
            .build()

        var interceptedRequest: Request? = null
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns dummyRequest
        every { chain.proceed(any()) } answers {
            interceptedRequest = firstArg()
            Response.Builder()
                .request(interceptedRequest!!)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }

        interceptor.intercept(chain)

        assertEquals("Bearer my-access-token", interceptedRequest?.header("Authorization"))
    }
}
