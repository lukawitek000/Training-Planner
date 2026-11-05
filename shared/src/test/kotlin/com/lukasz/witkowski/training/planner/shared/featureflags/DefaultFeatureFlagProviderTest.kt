package com.lukasz.witkowski.training.planner.shared.featureflags

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFeatureFlagProviderTest {

    @Test
    fun `isEnabled returns default value when no override is provided`() {
        val provider = DefaultFeatureFlagProvider()

        val isEnabled = provider.isEnabled(FeatureFlag.ONLINE_SYNC)

        assertFalse(isEnabled)
    }

    @Test
    fun `isEnabled returns override value when override is provided`() {
        val provider = DefaultFeatureFlagProvider(
            overrides = mapOf(FeatureFlag.ONLINE_SYNC to true)
        )

        val isEnabled = provider.isEnabled(FeatureFlag.ONLINE_SYNC)

        assertTrue(isEnabled)
    }

    @Test
    fun `observe emits current value of feature flag`() = runTest {
        val provider = DefaultFeatureFlagProvider(
            overrides = mapOf(FeatureFlag.ONLINE_SYNC to true)
        )

        provider.observe(FeatureFlag.ONLINE_SYNC).test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
}
