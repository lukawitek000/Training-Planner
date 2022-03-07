package com.lukasz.witkowski.training.planner.training.presentation

import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class TrainingPlanReceiverService : WearableListenerService() {

    @Inject
    lateinit var trainingPlanService: TrainingPlanService

    private var dataReceiverJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        dataReceiverJob = coroutineScope.launch {
            try {
                val inputStream = channelClient.getInputStream(channel).await()
                val outputStream = channelClient.getOutputStream(channel).await()
                if (inputStream != null && outputStream != null) {
                    trainingPlanService.receiveTrainingPlan(inputStream, outputStream)
                }
            } catch (e: CancellationException) {
                Timber.d("TrainingPlanReceiverService has been destroyed ${e.localizedMessage}")
            } catch (e: IOException) {
                Timber.d("Getting stream has failed ${e.localizedMessage}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataReceiverJob?.cancel()
    }

}