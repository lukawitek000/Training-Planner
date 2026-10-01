package com.lukasz.witkowski.training.planner

import android.app.Application
import com.lukasz.witkowski.training.planner.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class TrainingPlannerWearableApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@TrainingPlannerWearableApplication)
            modules(appModule)
        }
    }
}
