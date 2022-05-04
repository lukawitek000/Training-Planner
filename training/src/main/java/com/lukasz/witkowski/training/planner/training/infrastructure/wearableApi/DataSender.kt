package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import com.lukasz.witkowski.shared.utils.writeSuspending
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

internal class DataSender<T>(private val responseHandler: ResponseHandler<T>) {

    suspend fun sendData(
        data: T,
        outputStream: OutputStream,
        inputStream: InputStream
    ): SynchronizationResponse = withContext(Dispatchers.IO) {
        try {
            val byteArray = gson.toJson(data).toByteArray()
            outputStream.writeIntSuspending(byteArray.size)
            outputStream.writeSuspending(byteArray)
            val synchronizationResponse = readSynchronizationResponse(inputStream)
            responseHandler.handleSynchronizationResponse(data)
            SynchronizationResponse.SUCCESSFUL
        } catch (e: Exception) {
            e.printStackTrace()
            SynchronizationResponse.FAILURE
        }
    }

    private suspend fun readSynchronizationResponse(inputStream: InputStream): SynchronizationResponse {
        val response = inputStream.readSuspending()
        return SynchronizationResponse.values()[response]
    }
}
