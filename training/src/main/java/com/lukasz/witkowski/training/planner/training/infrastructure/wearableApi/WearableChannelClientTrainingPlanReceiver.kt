package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        return receiveTrainingPlanArray(numberOfTrainingPlans)
    }

    private fun receiveTrainingPlanArray(size: Int): Flow<TrainingPlan> = flow {
        for(i in 0 until size) {
            emit(receiveSingleTrainingPlan())
        }
    }

    private suspend fun receiveSingleTrainingPlan(): TrainingPlan {
        val sizeOfByteArray = inputStream.readSuspending()
        val buffer = ByteArray(sizeOfByteArray)
        inputStream.readSuspending(buffer) // Receiving TrainingPlan into buffer
        return gson.fromJson(String(buffer), TrainingPlan::class.java)
    }
}