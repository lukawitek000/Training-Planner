package com.lukasz.witkowski.training.planner.session.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId

internal class SessionService : Service(), SessionFinishedListener {

    val trainingSessionController by lazy { TrainingSessionController(applicationContext) }

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
        trainingSessionController.observeSessionState()
        trainingSessionController.addSessionFinishedListener(this)
    }

    override fun onDestroy() {
        trainingSessionController.destroy()
        trainingSessionController.removeSessionFinishedListener(this)
        super.onDestroy()
    }

    override fun onSessionFinished(trainingStatisticsId: TrainingStatisticsId) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
