package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_KEY
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import com.lukasz.witkowski.shared.utils.writeSuspending
import com.lukasz.witkowski.training.planner.repository.SyncDataRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@AndroidEntryPoint
class SendingDataService : LifecycleService() {

    @Inject
    lateinit var syncDataRepository: SyncDataRepository

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }

    override fun onCreate() {
        super.onCreate()
        observeNotSynchronizedData()
    }

    private fun observeNotSynchronizedData() {
        lifecycleScope.launch {
            syncDataRepository.getNotSynchronizedTrainings().collect {
                if (it.isNotEmpty()) {
                    Timber.d("Send trainings ${it.size} $it")
                    sendTrainings(it)
                }
            }
        }
    }

    private fun sendTrainings(trainings: List<TrainingWithExercises>) {
        lifecycleScope.launch {
            val nodeId = getConnectedNodes() ?: return@launch // TODO send to all nodes
            val channel = channelClient.openChannel(nodeId, TRAINING_PATH).await()
            val outputStream = channelClient.getOutputStream(channel).await()
            val inputStream = channelClient.getInputStream(channel).await()
            outputStream.writeIntSuspending(trainings.size)
            for (training in trainings) {
                sendSingleTraining(training, outputStream, inputStream)
            }
            outputStream.closeSuspending()
            inputStream.closeSuspending()
            channelClient.close(channel)
        }
    }

    private suspend fun getConnectedNodes(): String? {
        val nodeClient = Wearable.getNodeClient(this)
        val nodes = nodeClient.connectedNodes.await()
        Timber.d("Available nodes $nodes")
        return nodes.firstOrNull()?.id
    }


    private suspend fun sendSingleTraining(training: TrainingWithExercises, outputStream: OutputStream, inputStream: InputStream) {
        try {
            Timber.d("Send data")
            val byteArray = gson.toJson(training).toByteArray()
            outputStream.writeSuspending(byteArray)
            val syncResponse = inputStream.readSuspending()
            Timber.d("Message returned $syncResponse")
            if(syncResponse == SYNC_SUCCESSFUL) {
                // TODO change is synchronized to true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("Saving item failed ${e.localizedMessage}")
        }
    }
}