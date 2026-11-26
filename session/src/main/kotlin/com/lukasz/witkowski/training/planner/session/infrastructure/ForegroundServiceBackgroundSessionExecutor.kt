package com.lukasz.witkowski.training.planner.session.infrastructure

import android.content.Context
import android.content.Intent
import com.lukasz.witkowski.training.planner.session.application.BackgroundSessionExecutor
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingSessionNotificationController

class ForegroundServiceBackgroundSessionExecutor(
    private val context: Context,
    private val notificationController: TrainingSessionNotificationController
) : BackgroundSessionExecutor {
    override fun startExecution() {
        notificationController.cancelCompleted()
        context.startForegroundService(provideIntent())
    }

    override fun stopExecution() {
        context.stopService(provideIntent())
        notificationController.cancelAll()
    }

    private fun provideIntent() = Intent(
        context,
        TrainingSessionForegroundService::class.java
    )
}
