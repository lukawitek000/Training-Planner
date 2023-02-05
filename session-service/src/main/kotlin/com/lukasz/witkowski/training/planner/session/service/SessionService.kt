package com.lukasz.witkowski.training.planner.session.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import timber.log.Timber

class SessionService : Service() {
    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): SessionService = this@SessionService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    fun trainingId(): TrainingPlanId {
        return TrainingPlanId("TestId")
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Training session")
            .setContentTitle("Title of the training and description or reps")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = getString(R.string.session_service_channel_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.createNotificationChannel(channel)

    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "session_notification_channel"
        private const val NOTIFICATION_ID = 1
    }
}
