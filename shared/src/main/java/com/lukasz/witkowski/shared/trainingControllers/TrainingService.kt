package com.lukasz.witkowski.shared.trainingControllers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
abstract class TrainingService : LifecycleService() {

    companion object {
        const val TRAINING_ID_KEY = "TrainingIdKey"

        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID =
            "com.lukasz.witkowski.training.wearable.ONGOING_TRAINING"
        const val NOTIFICATION_CHANNEL_NAME = "Ongoing Training"
        const val NOTIFICATION_TITLE = "Training"
        const val NOTIFICATION_TEXT = "Training is active"
    }

    @Inject
    lateinit var timerHelper: TimerHelper

//    @Inject
//    lateinit var trainingRepository: TrainingRepository

//    @Inject
//    lateinit var trainingProgressController: TrainingProgressController

    protected var isStarted = false

    abstract fun buildNotification(trainingId: Long): Notification

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val trainingId = intent?.extras?.getLong(TRAINING_ID_KEY)!!
//        fetchTrainingWithExercises(trainingId)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(trainingId))
        return Service.START_STICKY
    }

    fun stopCurrentService() {
//        timerHelper.cancelTimer()
        stopForeground(true)
        stopSelf()
        isStarted = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
    }

    protected fun createNotificationBuilder(pendingIntent: PendingIntent?, icon: Int) =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_TEXT)
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

//    private fun fetchTrainingWithExercises(trainingId: Long) {
//        lifecycleScope.launch {
//            val trainingWithExercises = trainingRepository.fetchTrainingById(trainingId)
//            startTraining(trainingWithExercises)
//        }
//    }
//
//    open fun startTraining(trainingWithExercises: TrainingWithExercises) {
//        Timber.d("Start training")
//        trainingProgressController.startTraining(trainingWithExercises)
//        onTimerFinishedListener()
//        observeTrainingState()
//    }
//
//    private fun observeTrainingState() {
////        trainingProgressController.currentTrainingState.observe(this) {
////            when (it) {
////                is CurrentTrainingState.SummaryState -> {
////                    handleSummaryState()
////                }
////                is CurrentTrainingState.ExerciseState -> {
////                    handleExerciseState(it)
////                }
////                is CurrentTrainingState.RestTimeState -> {
////                    handleRestTimeState()
////                }
////            }
////        }
//    }
//
//    open fun handleSummaryState() {
//        timerHelper.cancelTimer()
//    }
//
//    open fun handleExerciseState(exerciseState: CurrentTrainingState.ExerciseState) {
//        timerHelper.cancelTimer()
//    }
//
//    open fun handleRestTimeState() {
//        timerHelper.cancelTimer()
//        timerHelper.startTimer(trainingProgressController.restTime)
//    }
//
//    private fun onTimerFinishedListener() {
//        timerHelper.timerFinished.observe(this) {
//            if (it) {
//                vibrate()
//                trainingProgressController.navigateToTheNextScreen()
//            }
//        }
//    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator.defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val vibrationTime = 300L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    vibrationTime,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(vibrationTime)
        }
    }
}