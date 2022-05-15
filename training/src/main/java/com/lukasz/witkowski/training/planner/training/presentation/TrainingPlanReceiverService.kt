package com.lukasz.witkowski.training.planner.training.presentation

import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.training.planner.synchronization.WearableChannelClientReceiver
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
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

    @Inject
    lateinit var trainingPlanService: TrainingPlanService
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }
    private lateinit var receiver: WearableChannelClientReceiver

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        coroutineScope.launch {
            val inputStream = channelClient.getInputStream(channel).await()
            val outputStream = channelClient.getOutputStream(channel).await()
            receiveTrainingPlan(inputStream, outputStream).collect {
                trainingPlanService.saveTrainingPlan(it)
                confirmReceivingTrainingPlan(it.id)
            }
        }
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
