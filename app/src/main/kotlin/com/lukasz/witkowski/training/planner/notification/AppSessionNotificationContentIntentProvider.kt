package com.lukasz.witkowski.training.planner.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.lukasz.witkowski.training.planner.MainActivity
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.SessionNotificationContentIntentProvider

class AppSessionNotificationContentIntentProvider(
    private val context: Context
) : SessionNotificationContentIntentProvider {
    override fun provideContentIntent(): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_ACTIVE_SESSION, true)
            }
        return PendingIntent.getActivity(
            context,
            CONTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val EXTRA_OPEN_ACTIVE_SESSION =
            "com.lukasz.witkowski.training.planner.extra.OPEN_ACTIVE_SESSION"
        private const val CONTENT_REQUEST_CODE = 1
    }
}
