package com.lukasz.witkowski.training.planner.session.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

internal class SessionService : Service() {

    private val trainingSessionController by lazy { TrainingSessionController(applicationContext) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    } }

    private val binder = LocalBinder()
    private var isStarted = false
    private val notificationFactory: NotificationFactory by lazy {
        SessionServiceContainer.getInstance().notificationFactory
    }
    private val trainingSessionPendingIntentFactory: TrainingSessionPendingIntentFactory by lazy {
        SessionServiceContainer.getInstance().trainingSessionPendingIntentFactory
    }

    override fun onBind(intent: Intent): IBinder {
        if (!isStarted) {
            isStarted = true
            startService(intent)
        }
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): SessionService = this@SessionService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = NotificationChannelFactory.create(this)
        val pendingIntent = trainingSessionPendingIntentFactory.create(
            this,
            trainingSessionController.trainingPlan.id
        )
        val notification = notificationFactory.create(this, channelId, pendingIntent)
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        trainingSessionController.onServiceDestroy()
    }

    override fun onDestroy() {
        trainingSessionController.onServiceCreate()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
