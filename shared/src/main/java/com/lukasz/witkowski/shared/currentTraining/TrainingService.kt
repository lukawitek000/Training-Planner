package com.lukasz.witkowski.shared.currentTraining

import androidx.lifecycle.LifecycleService
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrainingService : LifecycleService() {

    @Inject
    lateinit var timerHelper: TimerHelper

    @Inject
    lateinit var trainingProgressControllerFactory: TrainingProgressControllerFactory

    private lateinit var trainingProgressController: TrainingProgressController

    fun getTrainingProgressController(trainingWithExercises: TrainingWithExercises) {
        trainingProgressController = TrainingProgressController(trainingWithExercises)
    }



}