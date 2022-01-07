package com.lukasz.witkowski.training.planner.currentTraining.service

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
import androidx.health.services.client.data.CumulativeDataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.StatisticalDataPoint
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.lukasz.witkowski.shared.models.statistics.CaloriesStatistics
import com.lukasz.witkowski.shared.models.statistics.ExerciseStatistics
import com.lukasz.witkowski.shared.models.statistics.HeartRateStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingStatistics
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.currentTraining.CurrentTrainingActivity
import com.lukasz.witkowski.training.planner.currentTraining.CurrentTrainingState
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class TrainingService : LifecycleService() {

    @Inject
    lateinit var exerciseClient: ExerciseClient

    val currentTrainingProgressHelper: CurrentTrainingProgressHelper = CurrentTrainingProgressHelper
    val timerHelper = TimerHelper

    private val localBinder = LocalBinder()

    var trainingId = DEFAULT_TRAINING_ID
        private set

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

    private var exerciseState = ExerciseState.USER_ENDED
    private var caloriesCumulativeData: CumulativeDataPoint? = null
    private var heartRateStatisticalData: StatisticalDataPoint? = null
    private val _exerciseUpdatesEndedMessage = MutableLiveData("")
    val exerciseUpdatesEndedMessage: LiveData<String> = _exerciseUpdatesEndedMessage

    private var exercisesIdAndSetQueue: Queue<Pair<Long, Int>> = LinkedList()
    var trainingCompleteStatistics: TrainingCompleteStatistics? = null
        private set

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

    private fun buildNotification(): Notification {
        // TODO investigate notification
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
        initTrainingStatistics()
        Timber.d("Start training")
        currentTrainingProgressHelper.startTraining(trainingWithExercises)
        onTimerFinishedListener()
        observeTrainingState()
    }

    private fun observeTrainingState() {
        currentTrainingProgressHelper.currentTrainingState.observe(this) {
            when (it) {
                is CurrentTrainingState.SummaryState -> {
                    lifecycleScope.launch {
                        endExercise()
                    }
//                    stopCurrentService()
                    timerHelper.cancelTimer()
                }
                is CurrentTrainingState.ExerciseState -> {
                    Timber.d("Exercise state ${currentTrainingProgressHelper.currentSet} ${it.exercise}")
                    // TODO it is skipping everything training 1
                    exercisesIdAndSetQueue.offer(Pair(it.exercise.id, currentTrainingProgressHelper.currentSet))
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
            processExerciseUpdate(update)
        }

        override fun onLapSummary(lapSummary: ExerciseLapSummary) {
            Timber.d("On lap summary $lapSummary")
        }

    }

    private fun processExerciseUpdate(exerciseUpdate: ExerciseUpdate) {
        val oldState = exerciseState
        if(!oldState.isEnded && exerciseUpdate.state.isEnded) {
            // Exercise ended
            val exerciseTime = exerciseUpdate.activeDuration.toMillis()
            saveRecordedHealthStatistics(caloriesCumulativeData, heartRateStatisticalData, exerciseTime)
            when(exerciseUpdate.state) {
                ExerciseState.TERMINATED -> {
                    // Another app started tracking an exercise
                    _exerciseUpdatesEndedMessage.value = "Another app terminated exercise"
                }
                ExerciseState.AUTO_ENDED -> {
                    // The exercise was auto ended because there were no registered listeners
                    _exerciseUpdatesEndedMessage.value = "No registered listener"
                }
                ExerciseState.AUTO_ENDED_PERMISSION_LOST -> {
                    // Your exercises ended because it lost the required permissions
                    _exerciseUpdatesEndedMessage.value = "Required permissions lost"
                }
                ExerciseState.USER_ENDED -> {
                    // User ended exercise
                }
                else -> {}
            }
        }
        exerciseState = exerciseUpdate.state
        val aggregatedMetrics = exerciseUpdate.latestAggregateMetrics
        caloriesCumulativeData = (aggregatedMetrics[DataType.TOTAL_CALORIES] as? CumulativeDataPoint) ?: caloriesCumulativeData
        heartRateStatisticalData = (aggregatedMetrics[DataType.HEART_RATE_BPM] as? StatisticalDataPoint) ?: heartRateStatisticalData
    }

    private fun saveRecordedHealthStatistics(calories: CumulativeDataPoint?, heartRate: StatisticalDataPoint?, exerciseTime: Long) {
        // TODO extract to separate class
        if(trainingCompleteStatistics == null) {
            initTrainingStatistics()
        }
        val exercisesStatistics = trainingCompleteStatistics!!.exercisesStatistics.toMutableList()
        val currentCaloriesStatistics = CaloriesStatistics(calories?.total?.asDouble() ?: 0.0)
        val currentHeartRateStatistics = HeartRateStatistics(
            max = heartRate?.max?.asDouble() ?: 0.0,
            min = heartRate?.min?.asDouble() ?: 0.0,
            average = heartRate?.average?.asDouble() ?: 0.0
        )
        val (currentExerciseId, currentSet) = exercisesIdAndSetQueue.poll() ?: return
        val savedSets = if (currentSet - 1 == 0 ) 1 else currentSet - 1
        val index = exercisesStatistics.indexOfFirst { it.trainingExerciseId == currentExerciseId }
        if(index != -1) {
            val exerciseStatistics = exercisesStatistics[index]
            val heartRateStatistics = HeartRateStatistics(
                max = max(currentHeartRateStatistics.max, exerciseStatistics.heartRateStatistics.max),
                min = min(currentHeartRateStatistics.min, exerciseStatistics.heartRateStatistics.min),
                average = (currentHeartRateStatistics.average * savedSets + exerciseStatistics.heartRateStatistics.average) / currentSet
            )
            val caloriesStatistics = CaloriesStatistics(
                burntCalories = exerciseStatistics.burntCaloriesStatistics.burntCalories + currentCaloriesStatistics.burntCalories
            )
            val averageTime = (exerciseStatistics.averageTime * savedSets + exerciseTime) / currentSet
            exercisesStatistics.removeAt(index)
            exercisesStatistics.add(
                ExerciseStatistics(
                    id = exerciseStatistics.id,
                    trainingExerciseId = exerciseStatistics.trainingExerciseId,
                    heartRateStatistics = heartRateStatistics,
                    burntCaloriesStatistics = caloriesStatistics,
                    averageTime = averageTime
                )
            )
        } else {
            val exerciseStatistics = ExerciseStatistics(
                trainingExerciseId = currentExerciseId,
                heartRateStatistics = currentHeartRateStatistics,
                burntCaloriesStatistics = currentCaloriesStatistics,
                averageTime = exerciseTime
            )
            exercisesStatistics.add(exerciseStatistics)
        }
        trainingCompleteStatistics!!.exercisesStatistics = exercisesStatistics
    }

    private fun initTrainingStatistics() {
        trainingCompleteStatistics = TrainingCompleteStatistics(
            trainingStatistics = TrainingStatistics(
                trainingId = trainingId,
                date = System.currentTimeMillis()
            ),
            exercisesStatistics = emptyList()
        )
    }

    private suspend fun isExerciseInProgress(): Boolean {
        val exerciseInfo = exerciseClient.currentExerciseInfo.await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    private fun monitorHealthIndicators() {
        lifecycleScope.launch {
            endExercise()
            if(!isExerciseConfigured) {
                configureHealthServices()
            }
            exerciseConfig?.let {
                exerciseClient.startExercise(it).await()
            }
        }
    }

    private suspend fun endExercise() {
        if (isExerciseInProgress()) {
            exerciseClient.endExercise().await()
            trainingCompleteStatistics?.trainingStatistics?.totalTime = currentTrainingProgressHelper.trainingTime
        }
    }

    private suspend fun configureHealthServices() {
        val capabilities = exerciseClient.capabilities.await()
        val exerciseType = ExerciseType.WORKOUT
        if (exerciseType in capabilities.supportedExerciseTypes) {
            val exerciseCapabilities = capabilities.getExerciseTypeCapabilities(exerciseType)
            _isHeartRateSupported.value =
                DataType.HEART_RATE_BPM in exerciseCapabilities.supportedDataTypes
            _isBurntKcalSupported.value =
                DataType.TOTAL_CALORIES in exerciseCapabilities.supportedDataTypes

            exerciseClient.setUpdateListener(exerciseUpdateListener)
            // Types for which we want to receive aggregate metrics.
            val aggregateDataTypes = setOf(
                // "Total" here refers not to the aggregation but to basal + activity.
                DataType.TOTAL_CALORIES,
                DataType.HEART_RATE_BPM
            )
            exerciseConfig = ExerciseConfig.builder()
                .setExerciseType(exerciseType)
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
        timerHelper.cancelTimer()
        stopForeground(true)
        stopSelf()
        isStarted = false
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@TrainingService
    }

    private companion object {
        const val DEFAULT_TRAINING_ID = -1L
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL = "com.lukasz.witkowski.training.wearable.ONGOING_TRAINING"
        const val NOTIFICATION_CHANNEL_DISPLAY = "Ongoing Training"
        const val NOTIFICATION_TITLE = "Training"
        const val NOTIFICATION_TEXT = "Training is active"
        const val ONGOING_STATUS_TEMPLATE = "Ongoing Exercise #duration#"
    }
}
