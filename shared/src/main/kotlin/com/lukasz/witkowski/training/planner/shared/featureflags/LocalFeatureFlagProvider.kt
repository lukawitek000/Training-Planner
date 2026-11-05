package com.lukasz.witkowski.training.planner.shared.featureflags

import androidx.compose.runtime.staticCompositionLocalOf

val LocalFeatureFlagProvider = staticCompositionLocalOf<FeatureFlagProvider> {
    DefaultFeatureFlagProvider()
}
