package com.lukasz.witkowski.training.planner


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrainingPlannerApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}