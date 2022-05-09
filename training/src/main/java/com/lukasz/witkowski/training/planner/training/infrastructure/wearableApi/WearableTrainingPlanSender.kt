package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import android.content.Context
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers.TrainingPlanMapper
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class WearableTrainingPlanSender(private val context: Context) : TrainingPlanSender {

    private val sender = WearableChannelClientSender(context, TRAINING_PATH)

    override fun send(trainingPlans: List<TrainingPlan>): Flow<SynchronizationStatus> {
        Timber.d("Send data")
        val trainingPlansJson = trainingPlans.map { TrainingPlanMapper.toTrainingPlanJsonModel(it) }
        return sender.sendData(trainingPlansJson)
    }
}
