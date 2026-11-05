package com.lukasz.witkowski.training.planner.shared.featureflags

import org.koin.dsl.module

val featureFlagsModule = module {
    single<FeatureFlagProvider> { DefaultFeatureFlagProvider() }
}
