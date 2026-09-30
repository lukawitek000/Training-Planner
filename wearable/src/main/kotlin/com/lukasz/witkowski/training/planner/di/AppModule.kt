package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.session.service.SessionServiceContainer
import com.lukasz.witkowski.training.planner.statistics.di.statisticsModule
import com.lukasz.witkowski.training.planner.summary.TrainingSummaryViewModel
import com.lukasz.witkowski.training.planner.training.di.trainingModule
import com.lukasz.witkowski.training.planner.trainingSession.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.trainingSession.notification.OngoingActivityNotificationFactory
import com.lukasz.witkowski.training.planner.trainingSession.notification.WearableTrainingSessionPendingIntentFactory
import com.lukasz.witkowski.training.planner.traininglist.TrainingPlansListViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val appModule = module {
    includes(trainingModule, statisticsModule)
    single { initializeSessionServiceContainer() }
    viewModel<TrainingPlansListViewModel>()
    viewModel<TrainingSessionViewModel>()
    viewModel<TrainingSummaryViewModel>()
}

private fun initializeSessionServiceContainer(): SessionServiceContainer {
    val notificationPendingIntentProvider = WearableTrainingSessionPendingIntentFactory()
    val notificationFactory = OngoingActivityNotificationFactory()
    SessionServiceContainer.initialize(notificationPendingIntentProvider, notificationFactory)
    return SessionServiceContainer.getInstance()
}