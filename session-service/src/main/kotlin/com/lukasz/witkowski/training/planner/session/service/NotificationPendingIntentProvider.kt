package com.lukasz.witkowski.training.planner.session.service

import android.app.PendingIntent
import android.content.Context
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

interface NotificationPendingIntentProvider {
    fun provide(context: Context, trainingPlanId: TrainingPlanId): PendingIntent
}
