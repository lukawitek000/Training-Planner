package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.ChannelClient
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.service.DataLayerListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

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
