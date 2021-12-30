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
import androidx.concurrent.futures.await
import androidx.core.app.NotificationCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateListener
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrainingService : LifecycleService() {

    @Inject
    lateinit var currentTrainingRepository: CurrentTrainingRepository

    @Inject
    lateinit var exerciseClient: ExerciseClient

    val currentTrainingProgressHelper: CurrentTrainingProgressHelper = CurrentTrainingProgressHelper
    val timerHelper = TimerHelper

    private val localBinder = LocalBinder()

    private var trainingId = DEFAULT_TRAINING_ID

    private var isStarted = false

    private val _isHeartRateSupported = MutableLiveData(true)
    val isHeartRateSupported: LiveData<Boolean> = _isHeartRateSupported

    private val _isBurntKcalSupported = MutableLiveData(true)
    val isBurntKcalSupported: LiveData<Boolean> = _isBurntKcalSupported

    private val _isWorkoutExerciseSupported = MutableLiveData(true)
    val isWorkoutExerciseSupported: LiveData<Boolean> = _isWorkoutExerciseSupported

    private var exerciseConfig: ExerciseConfig? = null
    private val isExerciseConfigured: Boolean
        get() = exerciseConfig != null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("onStartCommand")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        return Service.START_STICKY
    }


    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Timber.d("onBind")
        if(!isStarted){
            isStarted = true
            startService(Intent(this, this::class.java))
        }
        return localBinder
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        currentTrainingProgressHelper.resetData()
        exerciseConfig = null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
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

    fun startTraining(trainingWithExercises: TrainingWithExercises) {
        this.trainingId = trainingWithExercises.training.id
        Timber.d("Start training")
        currentTrainingProgressHelper.startTraining(trainingWithExercises)
        onTimerFinishedListener()
        observeTrainingState()
    }

    private fun observeTrainingState() {
        currentTrainingProgressHelper.currentTrainingState.observe(this) {
            when (it) {
                is CurrentTrainingState.SummaryState -> {
                    stopCurrentService()
                }
                is CurrentTrainingState.ExerciseState -> {
                    exerciseClient.endExercise()
                    monitorHealthIndicators()
                }
                is CurrentTrainingState.RestTimeState -> {
                    exerciseClient.endExercise()
                }
            }
        }
    }

    private val exerciseUpdateListener = object : ExerciseUpdateListener {
        override fun onAvailabilityChanged(dataType: DataType, availability: Availability) {
            Timber.d("Availability changed $dataType $availability")
        }

        override fun onExerciseUpdate(update: ExerciseUpdate) {
            Timber.d("On exercise update $update")
        }

        override fun onLapSummary(lapSummary: ExerciseLapSummary) {
            Timber.d("On lap summary $lapSummary")
        }

    }

    private fun monitorHealthIndicators() {
        lifecycleScope.launch {
            if(!isExerciseConfigured) {
                configureHealthServices()
            }
            exerciseConfig?.let {
                exerciseClient.startExercise(it).await()
            }
        }
    }

    private suspend fun configureHealthServices() {
        val capabilities = exerciseClient.capabilities.await()
        val exerciseType = ExerciseType.WORKOUT
        Timber.d("Exercise type $exerciseType")
        if (exerciseType in capabilities.supportedExerciseTypes) {
            Timber.d("Exercise type is in capabilities supported types ${capabilities.supportedExerciseTypes}")
            val exerciseCapabilities = capabilities.getExerciseTypeCapabilities(exerciseType)
            _isHeartRateSupported.value =
                DataType.HEART_RATE_BPM in exerciseCapabilities.supportedDataTypes
            _isBurntKcalSupported.value =
                DataType.TOTAL_CALORIES in exerciseCapabilities.supportedDataTypes

            exerciseClient.setUpdateListener(exerciseUpdateListener)
            // Types for which we want to receive metrics.
            val dataTypes = setOf(
                DataType.HEART_RATE_BPM
            )
            // Types for which we want to receive aggregate metrics.
            val aggregateDataTypes = setOf(
                // "Total" here refers not to the aggregation but to basal + activity.
                DataType.TOTAL_CALORIES,
                DataType.HEART_RATE_BPM
            )
            exerciseConfig = ExerciseConfig.builder()
                .setExerciseType(exerciseType)
                .setDataTypes(dataTypes)
                .setAggregateDataTypes(aggregateDataTypes)
                .build()
        } else {
            _isWorkoutExerciseSupported.value = false
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

    fun stopCurrentService() {
        stopForeground(true)
        stopSelf()
        isStarted = false
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@TrainingService
    }

    private companion object {
        const val DEFAULT_TRAINING_ID = -1L
        const val DELAY = 1000L
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL = "com.lukasz.witkowski.training.wearable.ONGOING_TRAINING"
        const val NOTIFICATION_CHANNEL_DISPLAY = "Ongoing Training"
        const val NOTIFICATION_TITLE = "Training"
        const val NOTIFICATION_TEXT = "Training is active"
        const val ONGOING_STATUS_TEMPLATE = "Ongoing Exercise #duration#"
    }

}