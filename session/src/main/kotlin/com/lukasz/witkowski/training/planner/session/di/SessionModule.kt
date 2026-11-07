package com.lukasz.witkowski.training.planner.session.di

import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val sessionModule =
    module {
        includes(dispatchersModule)

        single<SessionStatisticsRecorder> { CompositeStatisticsRecorder() }
        single {
            val timeProvider = SystemTimeProvider()
            val backgroundDispatcher =
                get<kotlinx.coroutines.CoroutineDispatcher>(
                    named(CoroutineDispatcherQualifiers.DEFAULT)
                )
            TrainingSessionService(
                timeProvider = timeProvider,
                timerFactory =
                TimerFactory(
                    timeProvider = timeProvider,
                    timerDispatcher = backgroundDispatcher
                ),
                sessionCompletedListener = getOrNull<SessionCompletedListener>(),
                trainingSetsStrategy = StraightSetsPolicy(),
                backgroundDispatcher = backgroundDispatcher,
                sessionStatisticsRecorder = get()
            )
        }

        viewModel { params ->
            TrainingSessionViewModel(
                trainingPlanService = get(),
                trainingSessionService = get(),
                trainingPlanId = params.get()
            )
        }
    }
