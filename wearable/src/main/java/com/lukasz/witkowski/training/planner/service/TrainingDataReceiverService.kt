package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.ChannelClient
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.service.DataLayerListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TrainingDataReceiverService : DataLayerListenerService() {

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        currentChannel = channel
        coroutineScope.launch {
            currentChannel?.let { receiveData(it, TrainingWithExercises::class.java) }
        }
    }

    override suspend fun <T> handleReceivedData(data: T) {
        val trainingWithExercises = (data as? TrainingWithExercises) ?: return
        Timber.d("Received Training $trainingWithExercises")
        syncDataRepository.insertTrainingWithExercises(trainingWithExercises)
    }
}