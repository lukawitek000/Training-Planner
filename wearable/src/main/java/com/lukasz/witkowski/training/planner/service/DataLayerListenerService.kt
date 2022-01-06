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

    private val dataClient: DataClient by lazy { Wearable.getDataClient(this) }
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
            Timber.d("Waiting for bytes")
            val byteArray = readBytesSuspending(inputStream)

            Timber.d("Received ${byteArray.contentToString()}")
            try {
                val trainingWithExercises =
                    gson.fromJson(String(byteArray), TrainingWithExercises::class.java)
                Timber.d("Training with exercises $trainingWithExercises")

                outputStream.writeIntSuspending(1)

                Timber.d("Sent response")
            } catch (e: Exception) {
                Timber.d("Deserializing failed ${byteArray.contentToString()}")
            }
        } while (byteArray.isNotEmpty())
        outputStream.closeSuspending()
        inputStream.closeSuspending()
    }

    override fun onChannelClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
        super.onChannelClosed(p0, p1, p2)
        Timber.d("Channel closed $p0")
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Timber.d("Data changed")
        dataEvents.forEach { dataEvent ->
            if (dataEvent.type == DataEvent.TYPE_CHANGED && dataEvent.dataItem.uri.path == TRAINING_PATH) {
//                val time = DataMapItem.fromDataItem(dataEvent.dataItem)
//                    .dataMap
//                    .getLong(TRAINING_KEY)
                Timber.d("Received time")

                val trainingAsset = DataMapItem.fromDataItem(dataEvent.dataItem)
                    .dataMap
                    .getAsset(TRAINING_KEY)
                coroutineScope.launch {
                    val trainingWithExercises =
                        trainingAsset?.let { getTrainingWithExercisesFromAsset(it) }
                    Timber.d("Received $trainingWithExercises")
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroy service")
        coroutineScope.cancel()
    }

    private suspend fun getTrainingWithExercisesFromAsset(trainingAsset: Asset): TrainingWithExercises {
        val response = dataClient.getFdForAsset(trainingAsset).await()
        val byteArray = response.inputStream.readBytes()
        return gson.fromJson(String(byteArray), TrainingWithExercises::class.java)
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