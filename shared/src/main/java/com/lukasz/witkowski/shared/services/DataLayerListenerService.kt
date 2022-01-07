package com.lukasz.witkowski.shared.services

import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.lang.Exception

abstract class DataLayerListenerService : WearableListenerService() {
    protected val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }
    protected val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    abstract suspend fun receiveData(channel: ChannelClient.Channel)

    // Reads bytes till everything is received
    protected suspend fun readBytesSuspending(inputStream: InputStream) =
        withContext(Dispatchers.IO) {
            var arraySize = 0
            val listOfArrays = mutableListOf<ByteArray>()
            var totalBytes = 0
            do {
                arraySize += 256
                val temp = ByteArray(arraySize)
                var size = 0
                try {
                    size = inputStream.read(temp)
                    totalBytes += size
                    listOfArrays.add(temp)
                } catch (e: Exception) {
                    Timber.e("Failed reading ${e.localizedMessage}")
                    return@withContext byteArrayOf()
                }
            } while(size >= arraySize)
            val byteArray = ByteArray(totalBytes)
            var i = 0
            Timber.d("Byte array size $totalBytes")
            for(array in listOfArrays){
                for(byte in array) {
                    if(i >= totalBytes) return@withContext byteArray
                    byteArray[i] = byte
                    i++
                }
            }
            byteArray
        }
}