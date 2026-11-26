package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.app.PendingIntent

fun interface SessionNotificationContentIntentProvider {
    fun provideContentIntent(): PendingIntent
}
