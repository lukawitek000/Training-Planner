package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.training.planner.repository.SyncDataRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DataLayerListenerService: WearableListenerService() {

    @Inject
    lateinit var syncDataRepository: SyncDataRepository

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { dataEvent ->
            Timber.d("onDataChanged: event $dataEvent")
            if(dataEvent.type == DataEvent.TYPE_CHANGED) {
                dataEvent.dataItem.also { item ->
                    if(item.uri.path == STATISTICS_PATH) {
                        DataMapItem.fromDataItem(item).dataMap.apply {
                            handleReceivedStatistics(getByteArray(STATISTICS_KEY))
                        }
                    }
                }
            }
        }
    }

    private fun handleReceivedStatistics(byteArray: ByteArray?) {
        Timber.d("Received statistics")
    }

    companion object {
        private const val STATISTICS_PATH = "/statistics"
        private const val STATISTICS_KEY = "statistics"
    }
}