package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
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

    suspend fun sendReceivingConfirmation() {
        outputStream.writeIntSuspending(1)
    }

    private suspend fun <T> receiveSingleTrainingPlan(type: Class<T>): T {
        val sizeOfByteArray = inputStream.readSuspending()
        val buffer = ByteArray(sizeOfByteArray)
        inputStream.readSuspending(buffer)
        val stringBuffer = String(buffer)
        Timber.d("Received data $stringBuffer")
        return gson.fromJson(String(buffer), type)
    }
}
