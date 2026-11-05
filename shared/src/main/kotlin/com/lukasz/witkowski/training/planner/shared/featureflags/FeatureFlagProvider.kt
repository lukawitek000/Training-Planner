package com.lukasz.witkowski.training.planner.shared.featureflags

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface FeatureFlagProvider {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun observe(flag: FeatureFlag): Flow<Boolean>
}

class DefaultFeatureFlagProvider(private val overrides: Map<FeatureFlag, Boolean> = emptyMap()) :
    FeatureFlagProvider {
    override fun isEnabled(flag: FeatureFlag): Boolean = overrides[flag] ?: flag.defaultValue

    override fun observe(flag: FeatureFlag): Flow<Boolean> = flowOf(isEnabled(flag))
}
