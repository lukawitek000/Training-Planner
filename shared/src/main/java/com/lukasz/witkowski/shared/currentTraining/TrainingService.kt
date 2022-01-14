package com.lukasz.witkowski.shared.currentTraining

import android.app.Service
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.repository.TrainingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrainingService : LifecycleService() {

    companion object {
        const val TRAINING_ID_KEY = "TrainingIdKey"
    }

    @Inject
    lateinit var timerHelper: TimerHelper

    @Inject
    lateinit var trainingProgressControllerFactory: TrainingProgressControllerFactory

    @Inject
    lateinit var trainingRepository: TrainingRepository

    private lateinit var trainingProgressController: TrainingProgressController

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
            getTrainingProgressController(trainingWithExercises)
            startTraining()
        }
    }

    private fun startTraining() {
        Timber.d("Start training")
        trainingProgressController.startTraining()
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
//        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val vibrator = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
//            vibrator.defaultVibrator
//        } else {
//            getSystemService(VIBRATOR_SERVICE) as Vibrator
//        }
//        vibrator.vibrate(
//            VibrationEffect.createOneShot(
//                300,
//                VibrationEffect.DEFAULT_AMPLITUDE
//            )
//        )
    }

    private fun getTrainingProgressController(trainingWithExercises: TrainingWithExercises) {
        trainingProgressController = TrainingProgressController(trainingWithExercises)
    }
}