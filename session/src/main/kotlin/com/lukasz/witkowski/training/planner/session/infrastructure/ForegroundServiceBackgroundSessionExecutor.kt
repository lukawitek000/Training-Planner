package com.lukasz.witkowski.training.planner.session.infrastructure

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lukasz.witkowski.training.planner.session.application.BackgroundSessionExecutor
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingSessionNotificationController
import timber.log.Timber

class ForegroundServiceBackgroundSessionExecutor(
    private val context: Context,
    private val notificationController: TrainingSessionNotificationController
) : BackgroundSessionExecutor {
    override fun startExecution() {
        notificationController.cancelCompleted()
        try {
            context.startForegroundService(provideIntent())
        } catch (e: Exception) {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                e is ForegroundServiceStartNotAllowedException
            ) {
                Timber.w(e, "Cannot start workout foreground service from background")
            } else {
                throw e
            }
        }
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
