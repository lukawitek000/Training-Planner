package com.lukasz.witkowski.training.planner.shared.di

import com.lukasz.witkowski.training.planner.shared.time.SystemTimeProvider
import com.lukasz.witkowski.training.planner.shared.time.TimeProvider
import org.koin.dsl.module

val timeModule = module {
    single<TimeProvider> { SystemTimeProvider() }
}
