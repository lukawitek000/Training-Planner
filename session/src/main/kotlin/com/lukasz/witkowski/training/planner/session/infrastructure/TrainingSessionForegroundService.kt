package com.lukasz.witkowski.training.planner.session.infrastructure

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class TrainingSessionForegroundService : LifecycleService() {
    private val trainingSessionService: TrainingSessionService by inject()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        lifecycleScope.launch {
            trainingSessionService.trainingSessionState.collect {
                Timber.i("Collecting in service: $it")
            }
        }

        // TODO create notification channel, and subscribe to training session
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)
        try {
            val notification = createNotification()
            ServiceCompat.startForeground(
                this,
                1,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                },
            )
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                stopSessionService()
            } else {
                throw e
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO final clean up
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopSessionService()
    }

    private fun stopSessionService() {
        stopSelf()
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.exercise_default)
            .setContentTitle("Ongoing workout")
            .setContentText("Some content text")
            .setPriority(NotificationCompat.PRIORITY_LOW)
        return builder.build()
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        val notificationManager =
            applicationContext.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "TRAINING_SESSION_CHANNEL"
        const val CHANNEL_NAME = "Training Session"
    }
}
