package com.lukasz.witkowski.training.planner

import android.content.Context
import com.lukasz.witkowski.training.planner.exercise.di.ExerciseContainer
import com.lukasz.witkowski.training.planner.session.service.OngoingActivityNotificationFactory
import com.lukasz.witkowski.training.planner.session.service.SessionServiceContainer
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.training.di.TrainingContainer
import com.lukasz.witkowski.training.planner.trainingSession.WearableNotificationPendingIntentProvider

class AppContainer(private val context: Context) {
    val trainingContainer = TrainingContainer.getInstance(context)
    val statisticsContainer = StatisticsContainer.getInstance(context)

    init {
        initializeSessionServiceContainer()
    }

    private fun initializeSessionServiceContainer() {
        val notificationPendingIntentProvider = WearableNotificationPendingIntentProvider()
        val notificationFactory = OngoingActivityNotificationFactory()
        SessionServiceContainer.initialize(notificationPendingIntentProvider, notificationFactory)
    }
}
