package com.lukasz.witkowski.training.planner.shared.time

import androidx.compose.runtime.staticCompositionLocalOf

val LocalTimeProvider = staticCompositionLocalOf<TimeProvider> { SystemTimeProvider() }
