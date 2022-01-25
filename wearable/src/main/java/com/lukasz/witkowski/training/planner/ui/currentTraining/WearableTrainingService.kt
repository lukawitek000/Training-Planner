package com.lukasz.witkowski.training.planner.ui.currentTraining

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.wear.ongoing.OngoingActivity
import com.lukasz.witkowski.shared.currentTraining.CurrentTrainingState
import com.lukasz.witkowski.shared.currentTraining.TrainingService
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.planner.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WearableTrainingService: TrainingService() {

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 12
        const val TRAINING_ID_KEY = "trainingId"
    }

    @Inject
    lateinit var trainingStatisticsRecorder: TrainingStatisticsRecorder

    private val localBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService() = this@WearableTrainingService
    }

    override fun startTraining(trainingWithExercises: TrainingWithExercises) {
        super.startTraining(trainingWithExercises)
        trainingStatisticsRecorder.initTrainingStatistics(trainingWithExercises.training.id)
    }

    override fun handleSummaryState() {
        super.handleSummaryState()
        trainingStatisticsRecorder.finishExercise()
    }

    override fun handleExerciseState(exerciseState: CurrentTrainingState.ExerciseState) {
        super.handleExerciseState(exerciseState)
        val exerciseId = exerciseState.exercise.id
        val currentSet = trainingProgressController.currentSet
        trainingStatisticsRecorder.setLastExercise(trainingProgressController.isLastExercise())
        trainingStatisticsRecorder.startRecordingExerciseStatistics(exerciseId, currentSet)
    }

    override fun handleRestTimeState() {
        super.handleRestTimeState()
        trainingStatisticsRecorder.finishExercise()
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
        intent.putExtra(TRAINING_ID_KEY, trainingId)
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

    override fun onDestroy() {
        super.onDestroy()
        trainingStatisticsRecorder.cancelScope()
    }
}
