package com.lukasz.witkowski.training.planner.currentTraining

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.lukasz.witkowski.shared.currentTraining.TrainingService
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class WearableTrainingService: TrainingService() {


    private val localBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService() = this@WearableTrainingService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Timber.d("onBind")
        if(!isStarted){
            isStarted = true
            startService(intent)
        }
        return localBinder
    }

}