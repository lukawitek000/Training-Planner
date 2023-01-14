package com.lukasz.witkowski.training.planner

import android.app.Application
import timber.log.Timber

class TrainingPlannerWearableApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        appContainer = AppContainer(this)
    }
}
