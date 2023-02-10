package com.lukasz.witkowski.training.planner.session.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import timber.log.Timber

class SessionService : Service(), SessionFinishedListener {

    private val trainingSessionController by lazy { TrainingSessionController(applicationContext) }
    val timer: TimerController?
        get() = trainingSessionController.serviceTimerController

    private val binder = LocalBinder()
    private var isStarted = false
    private val notificationFactory: NotificationFactory by lazy { SessionServiceContainer.getInstance().notificationFactory }
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
        Timber.d("ondestroy service")
        trainingSessionController.destroy()
        trainingSessionController.removeSessionFinishedListener(this)
        super.onDestroy()
    }

    override fun onSessionFinished(trainingStatisticsId: TrainingStatisticsId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) throw IllegalStateException("Unsupported android sdk version")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
