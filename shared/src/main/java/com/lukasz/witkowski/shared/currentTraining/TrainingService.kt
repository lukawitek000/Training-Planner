package com.lukasz.witkowski.shared.currentTraining

import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrainingService : LifecycleService() {

    @Inject
    lateinit var timerHelper: TimerHelper

    @Inject
    lateinit var trainingProgressController: TrainingProgressController



}