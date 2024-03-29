package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import android.content.Context
import com.lukasz.witkowski.training.planner.synchronization.SynchronizationStatus
import com.lukasz.witkowski.training.planner.synchronization.TRAINING_PATH
import com.lukasz.witkowski.training.planner.synchronization.WearableChannelClientSender
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers.toTrainingPlanJsonModel
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel
import kotlinx.coroutines.flow.Flow

class WearableTrainingPlanSender(private val context: Context) : TrainingPlanSender {

    private val sender = WearableChannelClientSender<TrainingPlanJsonModel, TrainingPlanId>(
        context,
        TRAINING_PATH,
        getId = { getTrainingPlanId() }
    )

    override fun send(trainingPlans: List<TrainingPlan>): Flow<SynchronizationStatus<TrainingPlanId>> {
        val trainingPlansJson = trainingPlans.map { it.toTrainingPlanJsonModel() }
        return sender.sendData(trainingPlansJson)
    }

    private fun TrainingPlanJsonModel.getTrainingPlanId() = TrainingPlanId(id)
}
