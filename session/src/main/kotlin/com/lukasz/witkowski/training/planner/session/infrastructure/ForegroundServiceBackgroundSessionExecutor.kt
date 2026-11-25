package com.lukasz.witkowski.training.planner.session.infrastructure

import android.content.Context
import android.content.Intent
import com.lukasz.witkowski.training.planner.session.application.BackgroundSessionExecutor

class ForegroundServiceBackgroundSessionExecutor(
    private val context: Context,
) : BackgroundSessionExecutor {
    override fun startExecution() {
        context.startForegroundService(provideIntent())
    }

    override fun stopExecution() {
        context.stopService(provideIntent())
    }

    private fun provideIntent() =
        Intent(
            context,
            TrainingSessionForegroundService::class.java
        )
}