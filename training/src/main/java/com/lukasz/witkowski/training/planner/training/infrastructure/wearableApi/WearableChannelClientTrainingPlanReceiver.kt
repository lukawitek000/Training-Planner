package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanReceiver
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers.TrainingPlanMapper
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class WearableChannelClientTrainingPlanReceiver: TrainingPlanReceiver {

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    override suspend fun receiveTrainingPlan(
        inputStream: InputStream,
        outputStream: OutputStream
    ): Flow<TrainingPlan> {
        this.inputStream = inputStream
        this.outputStream = outputStream
        val numberOfTrainingPlans = inputStream.readSuspending()
        return receiveTrainingPlanArray(numberOfTrainingPlans).map { TrainingPlanMapper.toTrainingPlan(it) }
    }

    private fun receiveTrainingPlanArray(size: Int): Flow<TrainingPlanJsonModel> = flow {
        for(i in 0 until size) {
            emit(receiveSingleTrainingPlan())
        }
    }

    private suspend fun receiveSingleTrainingPlan(): TrainingPlanJsonModel {
        val sizeOfByteArray = inputStream.readSuspending()
        val buffer = ByteArray(sizeOfByteArray)
        inputStream.readSuspending(buffer) // Receiving TrainingPlan into buffer
        return gson.fromJson(String(buffer), TrainingPlanJsonModel::class.java)
    }
}