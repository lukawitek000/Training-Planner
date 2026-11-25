package com.lukasz.witkowski.training.planner.session.infrastructure

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class TrainingSessionForegroundService : LifecycleService() {
    private val trainingSessionService: TrainingSessionService by inject()
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }
    private data class NotificationSessionProgress(
        val progress: Int,
        val points: List<Int>,
        val exerciseName: String,
        val exerciseDetails: String,
    )
    private var progress = NotificationSessionProgress(0, emptyList(), "", "")

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        lifecycleScope.launch {
            trainingSessionService.trainingSessionState.collect { state ->
                Timber.i("Collecting in service: $state")
                when (state) {
                    is TrainingSessionState.IdleState -> Unit
                    is TrainingSessionState.SummaryState -> {
                        progress = progress.copy(progress = 100)
                    }
                    is TrainingSessionState.RestTimeState -> {}
                    is TrainingSessionState.ExerciseState -> {
                        progress = NotificationSessionProgress(
                            progress = state.progress.currentProgress(),
                            points = state.progress.points(),
                            exerciseName = state.sessionExercise.snapshot.name,
                            exerciseDetails = state.sessionExercise.snapshot.description,
                        )
                        notificationManager.notify(NOTIFICATION_ID, createNotification())
                    }
                }

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
        when (intent?.action) {
            SKIP_ACTION -> {
                trainingSessionService.skip()
                return START_STICKY
            }
            COMPLETE_ACTION -> {
                trainingSessionService.completed()
                return START_STICKY
            }
        }
        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        try {
            val notification = createNotification()
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
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
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO final clean up
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopSessionService()
    }

    private fun stopSessionService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val skipPendingIntent = createActionPendingIntent(SKIP_ACTION, requestCode = 2)
        val completePendingIntent = createActionPendingIntent(COMPLETE_ACTION, requestCode = 3)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val progressStyle = Notification.ProgressStyle()
                .setProgress(progress.progress)
                .setProgressPoints(
                    progress.points.also { Timber.i("Points: $it") }.map {
                        Notification.ProgressStyle.Point(it).setColor(Color.RED)
                    }
                )
            return Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.exercise_default)
                .setContentTitle(progress.exerciseName)
                .setContentText(progress.exerciseDetails)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSubText("Active session")
                .setStyle(progressStyle)
                .addAction(
                    Notification.Action.Builder(
                        Icon.createWithResource(this, R.drawable.ic_skip),
                        "Skip",
                        skipPendingIntent
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        Icon.createWithResource(this, R.drawable.ic_complete),
                        "Complete",
                        completePendingIntent
                    ).build()
                )
                .build()

        }
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.exercise_default)
            .setContentTitle(progress.exerciseName)
            .setContentText(progress.exerciseDetails)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        return builder.build()
    }

    private fun createActionPendingIntent(actionName: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, TrainingSessionForegroundService::class.java).apply {
            action = actionName
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        notificationManager.createNotificationChannel(channel)
    }

    private fun ExerciseQueueProgress.points(): List<Int> {
        val pointStep = 100 / totalExercises
        Timber.d("PointStep: $pointStep")
        return (0..100 step pointStep).toList()
    }

    private fun ExerciseQueueProgress.currentProgress(): Int {
        return ((totalExercises - remainingExercises) / totalExercises) * 100
    }

    private companion object {
        const val CHANNEL_ID = "TRAINING_SESSION_CHANNEL"
        const val CHANNEL_NAME = "Training Session"
        const val NOTIFICATION_ID = 123
        const val SKIP_ACTION = "SkipAction"
        const val COMPLETE_ACTION = "CompleteAction"
    }
}
