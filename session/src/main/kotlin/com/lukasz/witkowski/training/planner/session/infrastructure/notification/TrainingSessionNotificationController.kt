package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.app.Notification
import android.app.NotificationManager

class TrainingSessionNotificationController(
    private val notificationManager: NotificationManager,
    private val channelManager: TrainingNotificationChannelManager,
    private val notificationBuilder: TrainingSessionNotificationBuilder
) {
    val notificationId: Int = NOTIFICATION_ID

    fun initializeChannel() {
        channelManager.ensureChannelExists()
    }

    fun createNotification(uiState: TrainingNotificationUiState): Notification =
        notificationBuilder.build(uiState)

    fun update(uiState: TrainingNotificationUiState) {
        notificationManager.notify(notificationId, notificationBuilder.build(uiState))
    }

    private companion object {
        const val NOTIFICATION_ID = 123
    }
}
