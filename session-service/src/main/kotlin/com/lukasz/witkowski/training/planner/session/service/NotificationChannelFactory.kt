package com.lukasz.witkowski.training.planner.session.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat

internal object NotificationChannelFactory {
    private const val NOTIFICATION_CHANNEL_ID = "session_notification_channel"

    fun create(context: Context): String {
        val name = context.resources.getString(R.string.session_service_channel_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.createNotificationChannel(channel)
        return NOTIFICATION_CHANNEL_ID
    }
}
