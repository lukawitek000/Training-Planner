package com.lukasz.witkowski.training.planner.training.presentation

import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.shared.service.DataLayerListenerService
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrainingPlanReceiverService : WearableListenerService() {

    @Inject
    lateinit var trainingPlanService: TrainingPlanService


    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        coroutineScope.launch {
            val inputStream = channelClient.getInputStream(channel).await()
            val outputStream = channelClient.getOutputStream(channel).await()
            trainingPlanService.receiveTrainingPlan(inputStream, outputStream)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel("TrainingPlanReceiverService destroyed")
    }

}