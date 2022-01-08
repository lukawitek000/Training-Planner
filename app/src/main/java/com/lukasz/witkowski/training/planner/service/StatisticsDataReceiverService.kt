package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.repository.SyncDataRepository
import com.lukasz.witkowski.shared.services.DataLayerListenerService
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsDataReceiverService: DataLayerListenerService() {

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        currentChannel = channel
        coroutineScope.launch {
            currentChannel?.let { receiveData(it, TrainingCompleteStatistics::class.java) }
        }
    }

    override suspend fun <T> handleReceivedData(data: T) {
        val trainingCompleteStatistics = (data as? TrainingCompleteStatistics) ?: return
        Timber.d("Received statistics $trainingCompleteStatistics")
        syncDataRepository.insertTrainingCompleteStatistics(trainingCompleteStatistics)
    }
}
