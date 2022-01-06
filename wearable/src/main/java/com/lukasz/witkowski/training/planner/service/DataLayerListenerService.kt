package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.Channel
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.TRAINING_KEY
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.concurrent.timer

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
            val inputStream = channelClient.getInputStream(channel).await()
            Timber.d("Input channel")
            val byteArray = inputStream.read()
            Timber.d("Received $byteArray")
//            val trainingWithExercises = gson.fromJson(String(byteArray), TrainingWithExercises::class.java)
//            Timber.d("Training with exercises $trainingWithExercises")
        }
        Timber.d("After coroutine")

    }

    override fun onChannelClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
        super.onChannelClosed(p0, p1, p2)
        Timber.d("Channel closed $p0")
    }



    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Timber.d("Data changed")
        dataEvents.forEach { dataEvent ->
            if(dataEvent.type == DataEvent.TYPE_CHANGED && dataEvent.dataItem.uri.path == TRAINING_PATH) {
//                val time = DataMapItem.fromDataItem(dataEvent.dataItem)
//                    .dataMap
//                    .getLong(TRAINING_KEY)
                Timber.d("Received time")

                    val trainingAsset = DataMapItem.fromDataItem(dataEvent.dataItem)
                        .dataMap
                        .getAsset(TRAINING_KEY)
                coroutineScope.launch {
                    val trainingWithExercises = trainingAsset?.let { getTrainingWithExercisesFromAsset(it) }
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
        val byteArray =  response.inputStream.readBytes()
        return gson.fromJson(String(byteArray), TrainingWithExercises::class.java)
    }

}