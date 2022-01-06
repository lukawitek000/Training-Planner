package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.Asset
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

class DataLayerListenerService : WearableListenerService() {

    private val dataClient: DataClient by lazy { Wearable.getDataClient(this) }
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        Timber.d("Create service")
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
        trainingAsset.zza()
        val response = dataClient.getFdForAsset(trainingAsset).await()
        val byteArray =  response.inputStream.readBytes()
        return gson.fromJson(String(byteArray), TrainingWithExercises::class.java)
    }

}