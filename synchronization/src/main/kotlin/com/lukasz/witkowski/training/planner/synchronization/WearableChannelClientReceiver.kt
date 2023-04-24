package com.lukasz.witkowski.training.planner.synchronization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import java.io.OutputStream

class WearableChannelClientReceiver(
    private val inputStream: InputStream,
    private val outputStream: OutputStream
) {

    fun <T> receiveData(type: Class<T>): Flow<T> = flow {
        val numberOfItems = inputStream.readByteSuspending()
        (0 until numberOfItems).forEach {
            emit(receiveSingleTrainingPlan(type))
        }
    }

    suspend fun sendReceivingConfirmation() {
        outputStream.writeByteSuspending(ACKNOWLEDGE_FLAG)
    }

    private suspend fun <T> receiveSingleTrainingPlan(type: Class<T>): T {
        val sizeOfByteArray = getBufferSize()
        val buffer = ByteArray(sizeOfByteArray)
        inputStream.readByteSuspending(buffer)
        return gson.fromJson(String(buffer), type)
    }

    private suspend fun getBufferSize(): Int {
        return inputStream.readIntSuspending()
    }
}
