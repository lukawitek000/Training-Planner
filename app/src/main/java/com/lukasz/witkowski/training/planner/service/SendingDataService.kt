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
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
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
            val nodeId = getConnectedNodes() ?: return@launch
            val channel = channelClient.openChannel(nodeId, TRAINING_PATH).await()
            val outputStream = channelClient.getOutputStream(channel).await()
            val inputStream = channelClient.getInputStream(channel).await()
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
            Timber.d("Byte array ${byteArray.contentToString()}")
//            outputStream.write(12)
            outputStream.writeSuspending(byteArray)

            val message = inputStream.readSuspending()
            Timber.d("Message returned $message")


        } catch (cancellationException: CancellationException) {
            Timber.d("Job has been cancelled")
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("Saving item failed ${e.localizedMessage}")
        }
    }

    private suspend fun TrainingWithExercises.toAsset(): Asset =
        withContext(Dispatchers.Default) {
            val byteArray = gson.toJson(this@toAsset).toByteArray()
            Asset.createFromBytes(byteArray)
        }
}