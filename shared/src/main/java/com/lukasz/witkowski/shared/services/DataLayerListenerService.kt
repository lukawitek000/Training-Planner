package com.lukasz.witkowski.shared.services

import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.repository.SyncDataRepository
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
abstract class DataLayerListenerService : WearableListenerService() {

    @Inject
    lateinit var syncDataRepository: SyncDataRepository

    protected var currentChannel: ChannelClient.Channel? = null

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }
    protected val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    abstract suspend fun <T> handleReceivedData(data: T)

    protected suspend fun <T> receiveData(channel: ChannelClient.Channel, type: Class<T>) {
        val inputStream = channelClient.getInputStream(channel).await()
        val outputStream = channelClient.getOutputStream(channel).await()
        val numberOfItems = inputStream.readSuspending()
        Timber.d("Number of items $numberOfItems")
        for (i in 1..numberOfItems) {
            receiveSingleData(inputStream, outputStream, type)
        }
        outputStream.closeSuspending()
        inputStream.closeSuspending()
    }

    private suspend fun <T> receiveSingleData(
        inputStream: InputStream,
        outputStream: OutputStream,
        type: Class<T>
    ) {
        try {
            val byteArray = readBytesSuspending(inputStream)
            val receivedData =
                gson.fromJson(String(byteArray), type)
            Timber.d("Received data $receivedData")
            handleReceivedData(receivedData)
            outputStream.writeIntSuspending(SYNC_SUCCESSFUL)
        } catch (e: Exception) {
            Timber.d("Receiving data failed")
            outputStream.writeIntSuspending(SYNC_FAILURE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroy service")
        currentChannel?.let { channelClient.close(it) }
        coroutineScope.cancel()
    }

    // Reads bytes till everything is received
    private suspend fun readBytesSuspending(inputStream: InputStream) =
        withContext(Dispatchers.IO) {
            var arraySize = 0
            val listOfArrays = mutableListOf<ByteArray>()
            var totalBytes = 0
            do {
                arraySize += 256
                val temp = ByteArray(arraySize)
                var size = 0
                try {
                    size = inputStream.readSuspending(temp)
                    if (size == -1) {
                        break
                    }
                    totalBytes += size
                    listOfArrays.add(temp)
                } catch (e: Exception) {
                    Timber.e("Failed reading ${e.localizedMessage}")
                    return@withContext byteArrayOf()
                }
            } while (size >= arraySize)
            val byteArray = ByteArray(totalBytes)
            var i = 0
            Timber.d("Byte array size $totalBytes")
            for (array in listOfArrays) {
                for (byte in array) {
                    if (i >= totalBytes) return@withContext byteArray
                    byteArray[i] = byte
                    i++
                }
            }
            byteArray
        }
}