package com.lukasz.witkowski.training.planner.training.presentation

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers.TrainingPlanMapper
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class WearableTrainingPlanReceiver {

    private lateinit var receiver: WearableChannelClientReceiver

    fun receiveTrainingPlan(
        inputStream: InputStream,
        outputStream: OutputStream
    ): Flow<TrainingPlan> {
        receiver = WearableChannelClientReceiver(inputStream, outputStream)
        return receiver.receiveData(TrainingPlanJsonModel::class.java)
            .map { TrainingPlanMapper.toTrainingPlan(it) }
    }

    suspend fun confirmReceivingTrainingPlan(id: TrainingPlanId) {
        receiver.sendReceivingConfirmation()
    }
}
