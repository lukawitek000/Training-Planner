package com.lukasz.witkowski.training.wearable.currentTraining.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.currentTraining.CurrentTrainingActivity
import com.lukasz.witkowski.training.wearable.currentTraining.CurrentTrainingState
import com.lukasz.witkowski.training.wearable.repo.CurrentTrainingRepository
import com.lukasz.witkowski.training.wearable.startTraining.StartTrainingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrainingService : LifecycleService() {

    @Inject
    lateinit var currentTrainingRepository: CurrentTrainingRepository

    val currentTrainingProgressHelper: CurrentTrainingProgressHelper = CurrentTrainingProgressHelper
    val timerHelper = TimerHelper

    private val localBinder = LocalBinder()

    private var isBound = false
    private var isForeground = false

    private var trainingId = DEFAULT_TRAINING_ID


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("onStartCommand")

        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Timber.d("onBind")
        handleBind()
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Timber.d("onRebind")
        handleBind()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind")
        isBound = false
        lifecycleScope.launch {
            if(!isBound) {
                goForegroundOrStopSelf()
            }
        }
        return true
    }

    private fun goForegroundOrStopSelf() {
        lifecycleScope.launch {
            if(isExerciseRunning()) {
                showNotification()
            } else {
                Timber.d("Stop self")
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        currentTrainingProgressHelper.resetData()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
    }


    private fun isExerciseRunning(): Boolean {
        return true
    }

    private fun handleBind() {
        if(!isBound) {
            isBound = true
            startService(Intent(this, this::class.java))
            removeNotification()
        }
    }

    private fun showNotification() {
        if(!isForeground) {
            isForeground = true
            Timber.d("Show notification")
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun buildNotification(): Notification {
        val bundle = Bundle()
        bundle.putString("NotificationMessage", "notification")
        val intent = Intent(applicationContext, CurrentTrainingActivity::class.java)
        intent.putExtra("NotificationMessage", "notification")
        intent.putExtra(StartTrainingActivity.TRAINING_ID_KEY, trainingId)
        val pendingIntent = PendingIntent.getActivity(this, 12, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification.
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_TEXT)
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Ongoing Activity allows an ongoing Notification to appear on additional surfaces in the
        // Wear OS user interface, so that users can stay more engaged with long running tasks.

        val ongoingActivityStatus = Status.Builder()
            .addTemplate(ONGOING_STATUS_TEMPLATE)
            .addPart("duration", Status.StopwatchPart(10000))
            .build()
        val ongoingActivity =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setAnimatedIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setStaticIcon(R.drawable.common_full_open_on_phone)
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()
        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            NOTIFICATION_CHANNEL_DISPLAY,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
    }

    private fun removeNotification() {
        Timber.d("Remove notification")
        if (isForeground) {
            Timber.d( "Removing ongoing activity notification")
            isForeground = false
            stopForeground(true)
        }
    }

    fun startTraining(trainingWithExercises: TrainingWithExercises) {
        this.trainingId = trainingWithExercises.training.id
        Timber.d("Start training")
        currentTrainingProgressHelper.startTraining(trainingWithExercises)
        onTimerFinishedListener()
        observeTrainingState()
    }

    private fun observeTrainingState() {
        currentTrainingProgressHelper.currentTrainingState.observe(this) {
            if(it is CurrentTrainingState.SummaryState) {
                stopSelf()
            }
        }
    }

    private fun onTimerFinishedListener() {
        timerHelper.timerFinished.observe(this) {
            if (it) {
                vibrate()
                navigateToTheNextScreen()
            }
        }
    }

    private fun navigateToTheNextScreen() {
        if (currentTrainingProgressHelper.isExerciseState) {
            currentTrainingProgressHelper.navigateToTrainingRestTime()
            timerHelper.startTimer(currentTrainingProgressHelper.restTime)
        } else if (currentTrainingProgressHelper.isRestTimeState) {
            currentTrainingProgressHelper.navigateToTrainingExercise()
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator.defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                300,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    fun isTrainingStarted(): Boolean {
        return trainingId != DEFAULT_TRAINING_ID
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@TrainingService
    }

    private companion object {
        const val DEFAULT_TRAINING_ID = 1L
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL = "com.lukasz.witkowski.training.wearable.ONGOING_TRAINING"
        const val NOTIFICATION_CHANNEL_DISPLAY = "Ongoing Training"
        const val NOTIFICATION_TITLE = "Training"
        const val NOTIFICATION_TEXT = "Training is active"
        const val ONGOING_STATUS_TEMPLATE = "Ongoing Exercise #duration#"
    }

}