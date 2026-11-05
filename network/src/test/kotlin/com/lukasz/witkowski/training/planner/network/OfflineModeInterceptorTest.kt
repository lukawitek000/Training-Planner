package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.shared.featureflags.DefaultFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response

class OfflineModeInterceptorTest {
    private val request = Request.Builder()
        .url("https://example.com/api/v1/exercises")
        .build()

    @Test
    fun `intercept throws OfflineModeException when ONLINE_SYNC is false`() {
        val interceptor = OfflineModeInterceptor(
            DefaultFeatureFlagProvider(overrides = mapOf(FeatureFlag.ONLINE_SYNC to false))
        )
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request

        assertFailsWith<OfflineModeException> {
            interceptor.intercept(chain)
        }
        verify(exactly = 0) { chain.proceed(any()) }
    }

    @Test
    fun `intercept proceeds when ONLINE_SYNC is true`() {
        val interceptor = OfflineModeInterceptor(
            DefaultFeatureFlagProvider(overrides = mapOf(FeatureFlag.ONLINE_SYNC to true))
        )
        val expectedResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns expectedResponse

        val actualResponse = interceptor.intercept(chain)

        assertEquals(expectedResponse, actualResponse)
        verify(exactly = 1) { chain.proceed(request) }
    }
}
