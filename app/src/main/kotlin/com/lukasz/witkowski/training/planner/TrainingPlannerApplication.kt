package com.lukasz.witkowski.training.planner

import android.app.Application
import com.lukasz.witkowski.training.planner.di.appModule
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class TrainingPlannerApplication : Application() {
    private val syncScheduler: SyncScheduler by inject()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@TrainingPlannerApplication)
            modules(appModule)
        }
        syncScheduler.enqueueRemoteChangeSync(force = false)
        syncScheduler.enqueueMutationSync()
    }
}
