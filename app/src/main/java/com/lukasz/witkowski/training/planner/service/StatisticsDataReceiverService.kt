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

    @Inject
    lateinit var syncDataRepository: SyncDataRepository

    private var currentChannel: ChannelClient.Channel? = null

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        currentChannel = channel
        coroutineScope.launch {
            currentChannel?.let { receiveData(it) }
        }
    }

    override suspend fun receiveData(channel: ChannelClient.Channel) {
        val inputStream = channelClient.getInputStream(channel).await()
        val outputStream = channelClient.getOutputStream(channel).await()
        val numberOfStatistics = inputStream.readSuspending()
        Timber.d("Number of trainings $numberOfStatistics")
        receiveStatistics(numberOfStatistics, inputStream, outputStream)
        outputStream.closeSuspending()
        inputStream.closeSuspending()
    }

    private suspend fun receiveStatistics(
        numberOfStatistics: Int,
        inputStream: InputStream,
        outputStream: OutputStream
    ) {
        for (i in 1..numberOfStatistics) {
            Timber.d("Read bytes")
            try {
                val byteArray = readBytesSuspending(inputStream)
                Timber.d("Convert training with exercises")
                val trainingCompleteStatistics =
                    gson.fromJson(String(byteArray), TrainingCompleteStatistics::class.java)
                Timber.d("Training complete statistics $trainingCompleteStatistics")
                syncDataRepository.insertTrainingCompleteStatistics(trainingCompleteStatistics)
                outputStream.writeIntSuspending(SYNC_SUCCESSFUL)
            } catch (e: Exception) {
                Timber.d("Receiving data failed")
                outputStream.writeIntSuspending(SYNC_FAILURE)
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroy service")
        currentChannel?.let { channelClient.close(it) }
        coroutineScope.cancel()
    }

}