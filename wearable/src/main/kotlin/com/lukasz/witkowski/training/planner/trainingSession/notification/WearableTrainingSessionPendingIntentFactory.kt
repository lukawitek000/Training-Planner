package com.lukasz.witkowski.training.planner.trainingSession.notification

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import com.lukasz.witkowski.training.planner.session.service.TrainingSessionPendingIntentFactory
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.trainingSession.TrainingSessionActivity

class WearableTrainingSessionPendingIntentFactory: TrainingSessionPendingIntentFactory {
    override fun create(context: Context, trainingPlanId: TrainingPlanId): PendingIntent {
        val startActivityIntent = Intent(context, TrainingSessionActivity::class.java)
        startActivityIntent.putExtra(StartTrainingActivity.TRAINING_ID_KEY, trainingPlanId.toString())
        return  TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(startActivityIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }
}