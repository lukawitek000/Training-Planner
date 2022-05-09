package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanReceiver
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers.TrainingPlanMapper
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class WearableTrainingPlanReceiver : TrainingPlanReceiver {

    private lateinit var receiver: WearableChannelClientReceiver

    override suspend fun receiveTrainingPlan(
        inputStream: InputStream,
        outputStream: OutputStream
    ): Flow<TrainingPlan> {
        receiver = WearableChannelClientReceiver(inputStream, outputStream)
        return receiver.receiveData(TrainingPlanJsonModel::class.java)
            .map { TrainingPlanMapper.toTrainingPlan(it) }
    }
}

