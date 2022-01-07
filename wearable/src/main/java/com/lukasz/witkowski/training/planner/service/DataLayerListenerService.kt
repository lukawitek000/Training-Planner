package com.lukasz.witkowski.training.planner.service

import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_KEY
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import com.lukasz.witkowski.training.planner.repo.TrainingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class DataLayerListenerService : WearableListenerService() {

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Inject
    lateinit var repository: TrainingRepository

    private var currentChannel: ChannelClient.Channel? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Create service")
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Timber.d("Channel open $channel")
        currentChannel = channel
        coroutineScope.launch {
            currentChannel?.let { receiveData(it) }
        }
    }

    private suspend fun receiveData(channel: ChannelClient.Channel) {
        val inputStream = channelClient.getInputStream(channel).await()
        val outputStream = channelClient.getOutputStream(channel).await()
        val numberOfTrainings = inputStream.readSuspending()
        Timber.d("Number of trainings $numberOfTrainings")
        receiveTrainings(numberOfTrainings, inputStream, outputStream)
        outputStream.closeSuspending()
        inputStream.closeSuspending()
    }

    private suspend fun receiveTrainings(
        numberOfTrainings: Int,
        inputStream: InputStream,
        outputStream: OutputStream
    ) {
        for (i in 1..numberOfTrainings) {
            Timber.d("Read bytes")
            val byteArray = readBytesSuspending(inputStream)
            try {
                Timber.d("Convert training with exercises")
                val trainingWithExercises =
                    gson.fromJson(String(byteArray), TrainingWithExercises::class.java)
                Timber.d("Training with exercises $trainingWithExercises")
                repository.insertTrainingWithExercises(trainingWithExercises)
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

    // Reads bytes till everything is received
    private suspend fun readBytesSuspending(inputStream: InputStream) =
        withContext(Dispatchers.IO) {
            var arraySize = 0
            val listOfArrays = mutableListOf<ByteArray>()
            var totalBytes = 0
            do {
                arraySize += 256
                val temp = ByteArray(arraySize)
                val size = inputStream.read(temp)
                totalBytes += size
                listOfArrays.add(temp)
            } while(size >= arraySize)
            val byteArray = ByteArray(totalBytes)
            var i = 0
            Timber.d("Byte array size $totalBytes")
            for(array in listOfArrays){
                for(byte in array) {
                    if(i >= totalBytes) return@withContext byteArray
                    byteArray[i] = byte
                    i++
                }
            }
            byteArray
        }
}