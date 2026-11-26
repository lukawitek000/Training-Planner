package com.lukasz.witkowski.training.planner.session.infrastructure

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.SessionWeightUnitProvider
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingNotificationUiState
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingNotificationUiStateMapper
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.TrainingSessionNotificationController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import timber.log.Timber

class TrainingSessionForegroundService : LifecycleService() {
    private val trainingSessionService: TrainingSessionService by inject()
    private val notificationStateMapper: TrainingNotificationUiStateMapper by inject()
    private val notificationController: TrainingSessionNotificationController by inject()
    private val weightUnitProvider: SessionWeightUnitProvider by lazy {
        getKoin().getOrNull<SessionWeightUnitProvider>() ?: SessionWeightUnitProvider.Default
    }

    private var stateCollectionJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        notificationController.initializeChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundService()
        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopSessionService()
    }

    override fun onDestroy() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
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
            when (initialState) {
                is TrainingNotificationUiState.Idle -> {
                    if (!trainingSessionService.hasActiveSession) {
                        stopSessionService()
                    } else {
                        startObservingState()
                    }
                }
                is TrainingNotificationUiState.Summary -> {
                    notificationController.showCompleted(initialState)
                    stopSessionService()
                }
                is TrainingNotificationUiState.ActiveExercise,
                is TrainingNotificationUiState.Rest -> startObservingState()
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

    private fun startObservingState() {
        if (stateCollectionJob?.isActive == true) return
        stateCollectionJob =
            lifecycleScope.launch {
                combine(
                    trainingSessionService.trainingSessionState,
                    weightUnitProvider.weightUnitFlow()
                ) { state, weightUnit ->
                    notificationStateMapper.map(state, weightUnit)
                }
                    .distinctUntilChanged()
                    .collect { uiState ->
                        Timber.d("Notification UI state updated: %s", uiState)
                        when (uiState) {
                            is TrainingNotificationUiState.Idle -> {
                                if (!trainingSessionService.hasActiveSession) {
                                    stopSessionService()
                                }
                            }
                            is TrainingNotificationUiState.Summary -> {
                                notificationController.showCompleted(uiState)
                                stopSessionService()
                            }
                            is TrainingNotificationUiState.ActiveExercise,
                            is TrainingNotificationUiState.Rest -> {
                                notificationController.update(uiState)
                            }
                        }
                    }
            }
    }

    private fun stopSessionService() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
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
