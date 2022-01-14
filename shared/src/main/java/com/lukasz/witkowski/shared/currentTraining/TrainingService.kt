package com.lukasz.witkowski.shared.currentTraining

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.repository.TrainingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
open class TrainingService : LifecycleService() {

    companion object {
        const val TRAINING_ID_KEY = "TrainingIdKey"
    }

    @Inject
    lateinit var timerHelper: TimerHelper

    @Inject
    lateinit var trainingRepository: TrainingRepository

    @Inject
    lateinit var trainingProgressController: TrainingProgressController

    protected var isStarted = false

    fun stopCurrentService() {
        timerHelper.cancelTimer()
        stopForeground(true)
        stopSelf()
//        isStarted = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("onStartCommand")
        val trainingId = intent?.extras?.getLong(TRAINING_ID_KEY)!!
        fetchTrainingWithExercises(trainingId)
        return Service.START_STICKY
    }

    private fun fetchTrainingWithExercises(trainingId: Long) {
        lifecycleScope.launch {
            val trainingWithExercises = trainingRepository.fetchTrainingById(trainingId)
            startTraining(trainingWithExercises)
        }
    }

    private fun startTraining(trainingWithExercises: TrainingWithExercises) {
        Timber.d("Start training")
        trainingProgressController.startTraining(trainingWithExercises)
        onTimerFinishedListener()
        observeTrainingState()
    }

    private fun observeTrainingState() {
        trainingProgressController.currentTrainingState.observe(this) {
            when (it) {
                is CurrentTrainingState.SummaryState -> {
                    timerHelper.cancelTimer()
                }
                is CurrentTrainingState.ExerciseState -> {
                    timerHelper.cancelTimer()
                }
                is CurrentTrainingState.RestTimeState -> {
                    timerHelper.cancelTimer()
                    timerHelper.startTimer(trainingProgressController.restTime)
                }
            }
        }
    }

    private fun onTimerFinishedListener() {
        timerHelper.timerFinished.observe(this) {
            if (it) {
                vibrate()
                trainingProgressController.navigateToTheNextScreen()
            }
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator.defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val vibrationTime = 300L
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    vibrationTime,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(vibrationTime)
        }
    }
}