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
import com.lukasz.witkowski.shared.utils.TRAINING_KEY
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.training.planner.repository.SyncDataRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
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
            val nodeId = getConnectedNodes() ?: return@launch
            val channel = channelClient.openChannel(nodeId, TRAINING_PATH).await()
            val outputSteam = channelClient.getOutputStream(channel).await()
//            for (training in trainings) {
                sendSingleTraining(trainings.first(), outputSteam)
//            }
//            channelClient.close(channel)
        }
    }

    private suspend fun getConnectedNodes(): String? {
        val nodeClient = Wearable.getNodeClient(this)
        val nodes = nodeClient.connectedNodes.await()
        Timber.d("Available nodes $nodes")
        return nodes.firstOrNull()?.id
    }


    private suspend fun sendSingleTraining(training: TrainingWithExercises, outputStream: OutputStream) {
        try {
//            val data = training.toAsset()
//            val putDataRequest = PutDataMapRequest.create(TRAINING_PATH).apply {
//                dataMap.putAsset(TRAINING_KEY, data)
//            }
//                .asPutDataRequest()
//            val result = dataClient.putDataItem(putDataRequest)
            Timber.d("Send data")
//            val byteArray = gson.toJson(training).toByteArray()
            outputStream.write(12)
//            outputStream.flush()
//            result.addOnSuccessListener { Timber.d("On success sending") }
//            result.addOnFailureListener { Timber.d("On failure sending") }
//            result.addOnCanceledListener { Timber.d("On cancelled sending") }
//            result.addOnCompleteListener { Timber.d("On complete sending") }
//
//            result.await()
//            Timber.d("Result of sending $result")
        } catch (cancellationException: CancellationException) {
            Timber.d("Job has been cancelled")
        } catch (e: Exception) {
            Timber.d("Saving item failed")
        }
    }

    private suspend fun TrainingWithExercises.toAsset(): Asset =
        withContext(Dispatchers.Default) {
            val byteArray = gson.toJson(this@toAsset).toByteArray()
            Asset.createFromBytes(byteArray)
        }
}