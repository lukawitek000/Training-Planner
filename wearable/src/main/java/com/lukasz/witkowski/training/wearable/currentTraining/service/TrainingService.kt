package com.lukasz.witkowski.training.wearable.currentTraining.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TrainingService : LifecycleService() {

    private val localBinder = LocalBinder()

    private var isBound = false


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
            delay(UNBIND_DELAY_MILLIS)
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
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
    }


    private fun isExerciseRunning(): Boolean {
        return false
    }

    private fun handleBind() {
        if(!isBound) {
            isBound = true
            startService(Intent(this, this::class.java))
            removeNotification()
        }
    }

    private fun showNotification() {
        TODO("Not yet implemented")
    }

    private fun removeNotification() {
//        TODO("Not yet implemented")
        Timber.d("Remove notification")
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@TrainingService
    }

    private companion object {
        const val UNBIND_DELAY_MILLIS = 3_000L
    }

}