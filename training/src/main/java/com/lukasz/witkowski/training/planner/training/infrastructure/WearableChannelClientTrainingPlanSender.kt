package com.lukasz.witkowski.training.planner.training.infrastructure

import android.content.Context
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import com.lukasz.witkowski.shared.utils.writeSuspending
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

class WearableChannelClientTrainingPlanSender(private val context: Context): TrainingPlanSender {

    companion object {
        private const val SYNCHRONIZATION_FAILED = "Synchronization failed"
    }

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }

    override fun send(trainingPlans: List<TrainingPlan>): Flow<String> {
        Timber.d("Send data")
        return sendData(trainingPlans, TRAINING_PATH)
    }

    // TODO there can be separate class for methods (The same functionality will be shared between statistics module)

    private suspend fun getConnectedNodes(): List<String> {
        val nodeClient = Wearable.getNodeClient(context)
        val nodes = nodeClient.connectedNodes.await()
        return nodes.map { it.id }
    }

    private fun <T> sendData(data: List<T>, path: String): Flow<String> = flow {
        coroutineScope.launch {
            val nodesIds = getConnectedNodes()
            for (nodeId in nodesIds) {
                val channel = channelClient.openChannel(nodeId, path).await()
                val outputStream = channelClient.getOutputStream(channel).await()
                val inputStream = channelClient.getInputStream(channel).await()
                outputStream.writeIntSuspending(data.size)
                for (item in data) {
                    val synchronizedId = sendSingleData(
                        data = item,
                        outputStream = outputStream,
                        inputStream = inputStream
                    )
                    emit(synchronizedId)
                }
                outputStream.closeSuspending()
                inputStream.closeSuspending()
                channelClient.close(channel)
            }
        }
    }

    private suspend fun <T> sendSingleData(
        data: T,
        outputStream: OutputStream,
        inputStream: InputStream
    ): String = withContext(Dispatchers.IO) {
        try {
            val byteArray = gson.toJson(data).toByteArray()
            val job = exchangeDataAsync(outputStream, byteArray, inputStream)
            val id = getDataId(data)
            handleSyncResponse(id, job.await())
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("Saving item failed ${e.localizedMessage}")
            SYNCHRONIZATION_FAILED
        }
    }

    suspend fun handleSyncResponse(id: Long, syncResponse: Int): String {
        Timber.d("Sending training $id response $syncResponse")
        if(syncResponse == SYNC_SUCCESSFUL) {
//            syncDataRepository.updateSynchronizedTraining(id) // TODO mark data synchronization ??
            return id.toString()
        }
        throw Exception(SYNCHRONIZATION_FAILED)
    }

    private fun <T> getDataId(data: T): Long = when (data) {
        is TrainingPlan -> data.id.toLong()
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