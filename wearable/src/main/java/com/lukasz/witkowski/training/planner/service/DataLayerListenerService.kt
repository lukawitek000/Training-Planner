package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_KEY
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.lang.Exception

class DataLayerListenerService : WearableListenerService() {

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        Timber.d("Create service")
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        coroutineScope.launch {
            Timber.d("Input channel")
            receiveData(channel)
        }
        Timber.d("After coroutine")
    }

    private suspend fun receiveData(channel: ChannelClient.Channel) {
        val inputStream = channelClient.getInputStream(channel).await()
        val outputStream = channelClient.getOutputStream(channel).await()
        do {
            val byteArray = readBytesSuspending(inputStream)
            try {
                val trainingWithExercises =
                    gson.fromJson(String(byteArray), TrainingWithExercises::class.java)
                Timber.d("Training with exercises $trainingWithExercises")

                outputStream.writeIntSuspending(SYNC_SUCCESSFUL)
            } catch (e: Exception) {
                Timber.d("Receiving data failed")
                outputStream.writeIntSuspending(SYNC_FAILURE)
            }
        } while (byteArray.isNotEmpty())
        outputStream.closeSuspending()
        inputStream.closeSuspending()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroy service")
        coroutineScope.cancel()
    }

    private suspend fun readBytesSuspending(inputStream: InputStream) =
        withContext(Dispatchers.IO) {
            var arraySize = 0
            val listOfArrays = mutableListOf<ByteArray>()
            var totalBytes = 0
            do {
                arraySize += 256
                val temp = ByteArray(arraySize)
                val size = inputStream.read(temp)
                totalBytes += size
                listOfArrays.add(temp)
            } while(size >= arraySize)
            val byteArray = ByteArray(totalBytes)
            var i = 0
            Timber.d("")
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