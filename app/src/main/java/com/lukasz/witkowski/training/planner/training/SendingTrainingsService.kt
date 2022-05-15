package com.lukasz.witkowski.training.planner.training

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
// TODO What about registering service in manifest when it is in other module?
// TODO Is presentation layer appropriate for this purpose

/**
 * Responsible for sending training plans that are in the database and were not synchronized
 */
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
