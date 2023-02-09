package com.lukasz.witkowski.training.planner.trainingSession.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.session.service.NotificationFactory

class OngoingActivityNotificationFactory: NotificationFactory {

    override fun create(context: Context, channelId: String, pendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        val status = Status.Builder()
            .addTemplate(context.resources.getString(R.string.ongoing_notification_status))
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
