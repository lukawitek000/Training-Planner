package com.lukasz.witkowski.training.planner.session.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import timber.log.Timber

class SessionService : Service() {

    private lateinit var trainingSessionService: TrainingSessionService
    private val binder = LocalBinder()
    private var isStarted = false

    override fun onBind(intent: Intent): IBinder {
        if(!isStarted) {
            isStarted = true
            startService(intent)
        }
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): SessionService = this@SessionService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        setUpOngoingActivity()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
    }

    fun setUpService(trainingSessionService: TrainingSessionService, trainingPlan: TrainingPlan) {
        Timber.d("Set training session $trainingPlan")
        this.trainingSessionService = trainingSessionService
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


    private fun setUpOngoingActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setOngoing(true)
        val status = Status.Builder()
            .addTemplate("Running training")
            .build()
        val startActivityIntent = Intent(this, SessionService::class.java)
        val touchIntent = PendingIntent.getService(this, 1, startActivityIntent, PendingIntent.FLAG_IMMUTABLE)
        val ongoingActivity =
            OngoingActivity.Builder(this, ONGOING_ACTIVITY_NOTIFICATION_ID, notificationBuilder)
                .setStatus(status)
                .setTouchIntent(touchIntent)
                .build()
        ongoingActivity.apply(this)
        NotificationManagerCompat.from(this).run {
            notify(ONGOING_ACTIVITY_NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "session_notification_channel"
        private const val NOTIFICATION_ID = 1
        private const val ONGOING_ACTIVITY_NOTIFICATION_ID = 1
    }
}
