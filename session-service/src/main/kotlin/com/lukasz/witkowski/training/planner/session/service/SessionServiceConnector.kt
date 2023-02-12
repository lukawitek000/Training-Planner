package com.lukasz.witkowski.training.planner.session.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import timber.log.Timber

class SessionServiceConnector {

    private var sessionService: SessionService? = null
    var serviceConnected = false
    private var timerReadyCallback: (TimerController) -> Unit = {}
    private var sessionFinishedListener: SessionFinishedListener? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as SessionService.LocalBinder
            sessionService = binder.getService()
            serviceConnected = true
            sessionService?.timer?.let(timerReadyCallback)
            sessionFinishedListener?.let {
                sessionService!!.trainingSessionController.addSessionFinishedListener(it)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceConnected = false
        }
    }

    fun bindService(context: Context) {
        val intent = Intent(
            context.applicationContext,
            SessionService::class.java
        )
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        context.unbindService(connection)
    }

    // TODO some better way to get the timer, currently I assume that the service initializes new timer before the service is bound to fragment
    fun setTimerReadyCallback(callback: (TimerController) -> Unit) {
        this.timerReadyCallback = callback
    }
    // TODO same as above, I need this to navigate to summary screen
    fun addSessionFinishedListener(sessionFinishedListener: SessionFinishedListener) {
        this.sessionFinishedListener = sessionFinishedListener
    }
}
