package com.lukasz.witkowski.training.planner.session.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionStateMapper
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class SessionService : Service() {

    private val trainingSessionService: TrainingSessionService by lazy {
        StatisticsContainer.getInstance(applicationContext).trainingSessionService
    }

    private fun createServiceTimerController() = ServiceTimerController(StatisticsContainer.getInstance(applicationContext).timerController())

    var serviceTimerController: ServiceTimerController? = null
        private set
    private val coroutineScope =
        CoroutineScope(Dispatchers.Default + CoroutineName("SessionService"))

    private val binder = LocalBinder()
    private var isStarted = false

    override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind")
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
        Timber.d("onStartCommand")
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        setUpOngoingActivity()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        observeTrainingSessionState()
    }

    private fun observeTrainingSessionState() = coroutineScope.launch {
        trainingSessionService.trainingSessionState.map {
            TrainingSessionStateMapper.toPresentation(it)
        }.collectLatest {
            resetTimerHelper()
            when (it) {
                is TrainingSessionState.ExerciseState -> handleExerciseState(it)
                is TrainingSessionState.RestTimeState -> handleRestTimeState(it)
                is TrainingSessionState.SummaryState -> Unit
                is TrainingSessionState.IdleState -> Unit
            }
        }
    }

    private fun resetTimerHelper() {
        serviceTimerController?.cancel()
        serviceTimerController = null
    }

    private fun handleExerciseState(exerciseState: TrainingSessionState.ExerciseState) {
        serviceTimerController = createServiceTimerController()
        serviceTimerController?.setTimer(exerciseState.currentExercise.time)
        serviceTimerController?.observeHasFinished {
            serviceTimerController?.resetTimer()
        }
    }

    private fun handleRestTimeState(state: TrainingSessionState.RestTimeState) {
        serviceTimerController = createServiceTimerController()
        serviceTimerController?.setTimer(state.time)
        serviceTimerController?.startTimer()
        serviceTimerController?.observeHasFinished {
            trainingSessionService.skip()
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
        Timber.d("onDestroy")
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Training session")
            .setContentTitle("Title of the training and description or reps")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(notificationPendingIntentProvider!!.provide(this))
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
        val contentIntent = notificationPendingIntentProvider!!.provide(this)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(contentIntent)
            .setOngoing(true)
        val status = Status.Builder()
            .addTemplate("Running training")
            .build()
        val ongoingActivity =
            OngoingActivity.Builder(this, ONGOING_ACTIVITY_NOTIFICATION_ID, notificationBuilder)
                .setStatus(status)
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


        // TODO to late inject initialization this provider is  need in onstartcommand or even in oncreate
        var notificationPendingIntentProvider: NotificationPendingIntentProvider? = null
    }
}
