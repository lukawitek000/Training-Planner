package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.lukasz.witkowski.training.planner.session.R

class TrainingNotificationChannelManager(
    private val context: Context,
    private val notificationManager: NotificationManager
) {
    fun ensureChannelExists() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_training_session),
                NotificationManager.IMPORTANCE_LOW
            )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "TRAINING_SESSION_CHANNEL"
    }
}
