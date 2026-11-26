package com.lukasz.witkowski.training.planner.session.di

import android.app.NotificationManager
import com.lukasz.witkowski.training.planner.session.application.BackgroundSessionExecutor
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.session.infrastructure.ForegroundServiceBackgroundSessionExecutor
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingNotificationChannelManager
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingNotificationUiStateMapper
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingSessionNotificationBuilder
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingSessionNotificationController
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import com.lukasz.witkowski.training.planner.shared.di.timeModule
import com.lukasz.witkowski.training.planner.shared.time.TimeProvider
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val sessionModule =
    module {
        includes(dispatchersModule, timeModule)

        single<BackgroundSessionExecutor> {
            ForegroundServiceBackgroundSessionExecutor(
                context = androidApplication(),
                notificationController = get()
            )
        }
        factory { TrainingNotificationUiStateMapper() }
        single {
            val notificationManager =
                requireNotNull(
                    androidApplication().getSystemService(NotificationManager::class.java)
                )
            TrainingNotificationChannelManager(
                context = androidApplication(),
                notificationManager = notificationManager
            )
        }
        single {
            TrainingSessionNotificationBuilder(
                context = androidApplication(),
                contentIntentProvider = getOrNull()
            )
        }
        single {
            val notificationManager =
                requireNotNull(
                    androidApplication().getSystemService(NotificationManager::class.java)
                )
            TrainingSessionNotificationController(
                notificationManager = notificationManager,
                channelManager = get(),
                notificationBuilder = get()
            )
        }
        single<SessionStatisticsRecorder> { CompositeStatisticsRecorder() }
        single {
            val timeProvider = get<TimeProvider>()
            val backgroundDispatcher =
                get<kotlinx.coroutines.CoroutineDispatcher>(
                    named(CoroutineDispatcherQualifiers.DEFAULT)
                )
            val ioDispatcher =
                get<kotlinx.coroutines.CoroutineDispatcher>(
                    named(CoroutineDispatcherQualifiers.IO)
                )
            val sessionCompletedListener =
                getOrNull<SessionCompletedListener>() ?: SessionCompletedListener.NoOp

            TrainingSessionService(
                sessionCompletedListener = sessionCompletedListener,
                timeProvider = timeProvider,
                timerFactory =
                TimerFactory(
                    timeProvider = timeProvider,
                    timerDispatcher = backgroundDispatcher
                ),
                trainingSetsStrategy = StraightSetsPolicy(),
                backgroundDispatcher = backgroundDispatcher,
                ioDispatcher = ioDispatcher,
                sessionStatisticsRecorder = get(),
                backgroundSessionExecutor = get()
            )
        }

        viewModel { params ->
            TrainingSessionViewModel(
                trainingPlanService = get(),
                trainingSessionService = get(),
                exerciseService = get(),
                trainingPlanId = params.get()
            )
        }
    }
