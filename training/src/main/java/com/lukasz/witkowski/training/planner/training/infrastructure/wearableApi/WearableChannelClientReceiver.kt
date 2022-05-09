package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import java.io.OutputStream

class WearableChannelClientReceiver(
    private val inputStream: InputStream,
    private val outputStream: OutputStream
) {

    fun <T> receiveData(type: Class<T>): Flow<T> = flow {
        val numberOfItems = inputStream.readSuspending()
        for (i in 0 until numberOfItems) {
            emit(receiveSingleTrainingPlan(type))
        }
    }

    private suspend fun <T> receiveSingleTrainingPlan(type: Class<T>): T {
        val sizeOfByteArray = inputStream.readSuspending()
        val buffer = ByteArray(sizeOfByteArray)
        inputStream.readSuspending(buffer)
        return gson.fromJson(String(buffer), type)
    }
}
