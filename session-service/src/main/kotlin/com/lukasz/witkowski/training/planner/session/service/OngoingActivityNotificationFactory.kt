package com.lukasz.witkowski.training.planner.session.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status

class OngoingActivityNotificationFactory: NotificationFactory {

    override fun create(context: Context, channelId: String, pendingIntent: PendingIntent): Notification {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) throw IllegalStateException("Wrong sdk level") // not needed for wearable
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        val status = Status.Builder()
            .addTemplate("Training session running...")
            .build()
        val ongoingActivity = OngoingActivity.Builder(context, ONGOING_ACTIVITY_NOTIFICATION_ID, notificationBuilder)
                .setStatus(status)
                .build()
        ongoingActivity.apply(context)
        return notificationBuilder.build()
    }

    companion object {
        private const val ONGOING_ACTIVITY_NOTIFICATION_ID = 1
    }
}
