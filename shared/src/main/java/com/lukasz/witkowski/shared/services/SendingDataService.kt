package com.lukasz.witkowski.shared.services

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingWithStatistics
import com.lukasz.witkowski.shared.repository.SyncDataRepository
import com.lukasz.witkowski.shared.utils.STATISTICS_PATH
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import com.lukasz.witkowski.shared.utils.writeSuspending
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@AndroidEntryPoint
abstract class SendingDataService : LifecycleService() {

    @Inject
    lateinit var syncDataRepository: SyncDataRepository
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }

    abstract fun observeNotSynchronizedData()
    abstract suspend fun handleSyncResponse(id: Long, syncResponse: Int)

    private suspend fun getConnectedNodes(): String? {
        val nodeClient = Wearable.getNodeClient(this)
        val nodes = nodeClient.connectedNodes.await()
        Timber.d("Available nodes $nodes")
        return nodes.firstOrNull()?.id
    }

    override fun onCreate() {
        super.onCreate()
        observeNotSynchronizedData()
    }

    protected fun <T> sendData(data: List<T>, path: String) {
        lifecycleScope.launch {
            val nodeId = getConnectedNodes() ?: return@launch // TODO send to all nodes
            val channel = channelClient.openChannel(nodeId, path).await()
            val outputStream = channelClient.getOutputStream(channel).await()
            val inputStream = channelClient.getInputStream(channel).await()
            outputStream.writeIntSuspending(data.size)
            for(item in data) {
                sendSingleData(
                    data = item,
                    outputStream = outputStream,
                    inputStream = inputStream
                )
            }
            outputStream.closeSuspending()
            inputStream.closeSuspending()
            channelClient.close(channel)
        }
    }

    private suspend fun <T> sendSingleData(
        data: T,
        outputStream: OutputStream,
        inputStream: InputStream
    ) = withContext(Dispatchers.IO) {
        try {
            val byteArray = gson.toJson(data).toByteArray()
            val job = exchangeDataAsync(outputStream, byteArray, inputStream)
            val id = getDataId(data)
            handleSyncResponse(id, job.await())
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("Saving item failed ${e.localizedMessage}")
        }
    }

    private fun <T> getDataId(data: T): Long = when(data) {
        is TrainingWithExercises -> data.training.id
        is TrainingCompleteStatistics -> data.trainingStatistics.id
        else -> -1
    }

    private fun CoroutineScope.exchangeDataAsync(
        outputStream: OutputStream,
        byteArray: ByteArray,
        inputStream: InputStream
    ): Deferred<Int> {
        val job: Deferred<Int> = async {
            try {
                outputStream.writeSuspending(byteArray)
                inputStream.readSuspending()
            } catch (e: Exception) {
                Timber.d("Error during exchanging messages occurred ${e.localizedMessage}")
                SYNC_FAILURE
            }
        }
        return job
    }
}