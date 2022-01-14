package com.lukasz.witkowski.training.planner.currentTraining

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.lukasz.witkowski.shared.currentTraining.TrainingService
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class WearableTrainingService: TrainingService() {

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 12
    }

    private val localBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService() = this@WearableTrainingService
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
        val intent = Intent(applicationContext, CurrentTrainingActivity::class.java)
        intent.putExtra(StartTrainingActivity.TRAINING_ID_KEY, trainingId)
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification.
        val notificationBuilder = createNotificationBuilder(pendingIntent, R.drawable.logo)

        // Ongoing Activity allows an ongoing Notification to appear on additional surfaces in the
        // Wear OS user interface, so that users can stay more engaged with long running tasks.
        val ongoingActivity =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(R.drawable.logo)
                .setTouchIntent(pendingIntent)
                .build()
        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }
}
