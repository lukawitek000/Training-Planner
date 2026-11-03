package com.lukasz.witkowski.training.planner.session.di

import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.Timer
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

val sessionModule =
    module {
        includes(dispatchersModule)
        single {
            TrainingSessionService(
                timeProvider = SystemTimeProvider(),
                timer = Timer(),
                sessionCompletedListener = getOrNull<SessionCompletedListener>(),
                trainingSetsStrategy = CircuitSetsPolicy(),
                backgroundDispatcher = get(named(CoroutineDispatcherQualifiers.DEFAULT))
            )
        }
    }
