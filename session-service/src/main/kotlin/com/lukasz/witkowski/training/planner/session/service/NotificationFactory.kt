package com.lukasz.witkowski.training.planner.session.service

import android.app.Notification
import android.content.Context

interface NotificationFactory {
    fun create(context: Context, channelId: String): Notification
}
