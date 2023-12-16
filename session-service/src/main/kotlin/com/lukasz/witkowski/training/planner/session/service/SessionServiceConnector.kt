package com.lukasz.witkowski.training.planner.session.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class SessionServiceConnector {

    private var sessionService: SessionService? = null
    var serviceConnected = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as SessionService.LocalBinder
            sessionService = binder.getService()
            serviceConnected = true
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
}
