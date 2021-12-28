package com.lukasz.witkowski.training.wearable.currentTraining.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import timber.log.Timber

class TrainingServiceConnection : ServiceConnection {

    var trainingService: TrainingService? = null
        private set

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        trainingService = (service as TrainingService.LocalBinder).getService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.d("Training service disconnected")
    }
}