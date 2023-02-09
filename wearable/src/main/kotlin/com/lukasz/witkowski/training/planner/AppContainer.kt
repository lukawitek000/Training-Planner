package com.lukasz.witkowski.training.planner

import android.content.Context
import com.lukasz.witkowski.training.planner.trainingSession.notification.OngoingActivityNotificationFactory
import com.lukasz.witkowski.training.planner.session.service.SessionServiceContainer
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.training.di.TrainingContainer
import com.lukasz.witkowski.training.planner.trainingSession.notification.WearableTrainingSessionPendingIntentFactory

class AppContainer(context: Context) {
    val trainingContainer = TrainingContainer.getInstance(context)
    val statisticsContainer = StatisticsContainer.getInstance(context)

    init {
        initializeSessionServiceContainer()
    }

    private fun initializeSessionServiceContainer() {
        val notificationPendingIntentProvider = WearableTrainingSessionPendingIntentFactory()
        val notificationFactory = OngoingActivityNotificationFactory()
        SessionServiceContainer.initialize(notificationPendingIntentProvider, notificationFactory)
    }
}
