package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.app.Notification
import android.app.NotificationManager

class TrainingSessionNotificationController(
    private val notificationManager: NotificationManager,
    private val channelManager: TrainingNotificationChannelManager,
    private val notificationBuilder: TrainingSessionNotificationBuilder
) {
    val notificationId: Int = ACTIVE_NOTIFICATION_ID

    fun initializeChannel() {
        channelManager.ensureChannelExists()
    }

    fun createNotification(uiState: TrainingNotificationUiState): Notification =
        notificationBuilder.build(uiState)

    fun update(uiState: TrainingNotificationUiState) {
        notificationManager.notify(ACTIVE_NOTIFICATION_ID, notificationBuilder.build(uiState))
    }

    fun showCompleted(uiState: TrainingNotificationUiState.Summary) {
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notificationBuilder.build(uiState))
    }

    fun cancelActive() {
        notificationManager.cancel(ACTIVE_NOTIFICATION_ID)
    }

    fun cancelCompleted() {
        notificationManager.cancel(COMPLETION_NOTIFICATION_ID)
    }

    fun cancelAll() {
        cancelActive()
        cancelCompleted()
    }

    private companion object {
        const val ACTIVE_NOTIFICATION_ID = 123
        const val COMPLETION_NOTIFICATION_ID = 124
    }
}
