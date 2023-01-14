package com.lukasz.witkowski.training.planner.training.presentation

import android.content.Context
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.training.planner.synchronization.WearableChannelClientReceiver
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.di.TrainingContainer
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers.TrainingPlanMapper
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@AndroidEntryPoint
class TrainingPlanReceiverService : WearableListenerService() {

    private val trainingPlanService: TrainingPlanService by lazy {
        TrainingContainer.getInstance(applicationContext).service
    }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }
    private lateinit var receiver: WearableChannelClientReceiver

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        coroutineScope.launch {
            val (inputStream, outputStream) = openStreams(channel)
            receiveTrainingPlan(inputStream, outputStream).collect {
                trainingPlanService.saveTrainingPlan(it)
                confirmReceivingTrainingPlan(it.id)
            }
        }
    }

    private suspend fun openStreams(channel: ChannelClient.Channel): Pair<InputStream, OutputStream> {
        val inputStream = channelClient.getInputStream(channel).await()
        val outputStream = channelClient.getOutputStream(channel).await()
        return Pair(inputStream, outputStream)
    }

    private fun receiveTrainingPlan(
        inputStream: InputStream,
        outputStream: OutputStream
    ): Flow<TrainingPlan> {
        receiver = WearableChannelClientReceiver(inputStream, outputStream)
        return receiver.receiveData(TrainingPlanJsonModel::class.java)
            .map { TrainingPlanMapper.toTrainingPlan(it) }
    }

    private suspend fun confirmReceivingTrainingPlan(id: TrainingPlanId) {
        receiver.sendReceivingConfirmation()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel("TrainingPlanReceiverService destroyed")
    }
}
