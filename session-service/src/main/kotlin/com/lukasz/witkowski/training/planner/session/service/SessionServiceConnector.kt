package com.lukasz.witkowski.training.planner.session.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.presentation.CoroutinesTimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionServiceConnector {

    private var sessionService: SessionService? = null
    var serviceConnected = false
    var notificationPendingIntentProvider: NotificationPendingIntentProvider? = null
    private val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as SessionService.LocalBinder
            sessionService = binder.getService()
            serviceConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceConnected = false
        }
    }

    // on start
    fun bindService(context: Context) {
        notificationPendingIntentProvider?.let { SessionService.notificationPendingIntentProvider = it }
        val intent = Intent(
            context.applicationContext,
            SessionService::class.java
        )
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // on stop
    fun unbindService(context: Context) {
        context.unbindService(connection)
    }

    fun stopService() {
        sessionService!!.stopSelf()
    }

    suspend fun restTimeTimer(): TimerController = withContext(Dispatchers.Default) {
        while (sessionService == null) {
            // waiting for service connection
        }
        sessionService!!.restTimeTimer
    }
}
