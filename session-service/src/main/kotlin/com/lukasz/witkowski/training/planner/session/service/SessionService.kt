package com.lukasz.witkowski.training.planner.session.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

class SessionService: Service() {
    private val binder = LocalBinder()

    fun trainingId(): TrainingPlanId {
        return TrainingPlanId("TestId")
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder: Binder() {
        fun getService(): SessionService = this@SessionService
    }
}
