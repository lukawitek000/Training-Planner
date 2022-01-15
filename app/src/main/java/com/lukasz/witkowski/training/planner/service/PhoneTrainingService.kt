package com.lukasz.witkowski.training.planner.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.lukasz.witkowski.shared.currentTraining.TrainingService
import com.lukasz.witkowski.training.planner.MainActivity
import com.lukasz.witkowski.training.planner.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneTrainingService : TrainingService() {

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 11
    }

    private val localBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService() = this@PhoneTrainingService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        if(!isStarted){
            isStarted = true
            startService(intent)
        }
        return localBinder
    }

    override fun buildNotification(trainingId: Long): Notification {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(TRAINING_ID_KEY, trainingId)
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification.
        val notificationBuilder = createNotificationBuilder(pendingIntent, R.drawable.logo)
        return notificationBuilder.build()
    }
}