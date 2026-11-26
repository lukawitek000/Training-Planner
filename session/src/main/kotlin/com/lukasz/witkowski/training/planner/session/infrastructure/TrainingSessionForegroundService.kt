package com.lukasz.witkowski.training.planner.session.infrastructure

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingNotificationAction
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingNotificationUiState
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingNotificationUiStateMapper
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingSessionNotificationController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class TrainingSessionForegroundService : LifecycleService() {
    private val trainingSessionService: TrainingSessionService by inject()
    private val notificationStateMapper: TrainingNotificationUiStateMapper by inject()
    private val notificationController: TrainingSessionNotificationController by inject()

    private var isForegroundStarted = false

    override fun onCreate() {
        super.onCreate()
        notificationController.initializeChannel()
        lifecycleScope.launch {
            trainingSessionService.trainingSessionState
                .map(notificationStateMapper::map)
                .distinctUntilChanged()
                .collect { uiState ->
                    Timber.d("Notification UI state updated: %s", uiState)
                    if (!isForegroundStarted) return@collect
                    when (uiState) {
                        is TrainingNotificationUiState.Idle -> stopSessionService()
                        is TrainingNotificationUiState.Summary -> {
                            notificationController.showCompleted(uiState)
                            stopSessionService()
                        }
                        is TrainingNotificationUiState.ActiveExercise,
                        is TrainingNotificationUiState.Rest -> notificationController.update(
                            uiState
                        )
                    }
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val action = TrainingNotificationAction.fromIntentAction(intent?.action)
        if (action != null) {
            val currentState = trainingSessionService.trainingSessionState.value
            if (currentState is TrainingSessionState.IdleState) {
                Timber.w("Ignoring notification action %s for idle session", action)
                stopSessionService()
                return START_NOT_STICKY
            }
            when (action) {
                TrainingNotificationAction.SKIP -> trainingSessionService.skip()
                TrainingNotificationAction.COMPLETE -> trainingSessionService.completed()
                TrainingNotificationAction.START_TIMER -> {
                    if (
                        currentState is TrainingSessionState.ExerciseState &&
                        currentState.timerState is TimerState.Ready
                    ) {
                        trainingSessionService.startTimer()
                    }
                }
                TrainingNotificationAction.RESET_TIMER -> {
                    if (currentState is TrainingSessionState.ExerciseState) {
                        trainingSessionService.resetTimer()
                    }
                }
            }
            if (!isForegroundStarted) {
                startForegroundService()
            }
            return START_STICKY
        }

        startForegroundService()
        return START_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopSessionService()
    }

    override fun onDestroy() {
        isForegroundStarted = false
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationController.cancelActive()
        super.onDestroy()
    }

    private fun startForegroundService() {
        val initialState =
            notificationStateMapper.map(trainingSessionService.trainingSessionState.value)
        notificationController.cancelCompleted()
        val notification = notificationController.createNotification(initialState)
        try {
            ServiceCompat.startForeground(
                this,
                notificationController.notificationId,
                notification,
                foregroundServiceType()
            )
            isForegroundStarted = true
            when (initialState) {
                is TrainingNotificationUiState.Idle -> stopSessionService()
                is TrainingNotificationUiState.Summary -> {
                    notificationController.showCompleted(initialState)
                    stopSessionService()
                }
                is TrainingNotificationUiState.ActiveExercise,
                is TrainingNotificationUiState.Rest -> Unit
            }
        } catch (e: Exception) {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                e is ForegroundServiceStartNotAllowedException
            ) {
                Timber.w(e, "Foreground service start not allowed, stopping service")
                stopSessionService()
            } else {
                throw e
            }
        }
    }

    private fun stopSessionService() {
        isForegroundStarted = false
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationController.cancelActive()
        stopSelf()
    }

    private fun foregroundServiceType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
}
