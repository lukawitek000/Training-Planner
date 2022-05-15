package com.lukasz.witkowski.training.planner.training

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SendingTrainingsService : LifecycleService() {

    @Inject
    lateinit var trainingPlanService: TrainingPlanService

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            trainingPlanService.sendNotSynchronizedTrainingPlans()
        }
    }

}
