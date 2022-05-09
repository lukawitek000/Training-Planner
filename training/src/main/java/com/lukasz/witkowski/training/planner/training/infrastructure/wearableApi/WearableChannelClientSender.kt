package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import android.content.Context
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import com.lukasz.witkowski.shared.utils.writeSuspending
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class WearableChannelClientSender(private val context: Context, private val path: String) {

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    /**
     * If sending data has failed the [SynchronizationStatus.FailureSynchronization] is emitted,
     * otherwise the [SynchronizationStatus.SuccessfulSynchronization] with id of the successfully synchronized object
     */
    fun <T> sendData(data: List<T>): Flow<SynchronizationStatus> = flow {
        val nodesIds = getConnectedNodesIds()
        sendDataToEachNode(nodesIds, data)
    }

    private suspend fun getConnectedNodesIds(): List<String> {
        val nodeClient = Wearable.getNodeClient(context)
        val nodes = nodeClient.connectedNodes.await()
        return nodes.map { it.id }
    }

    private suspend fun <T> FlowCollector<SynchronizationStatus>.sendDataToEachNode(
        nodesIds: List<String>,
        data: List<T>
    ) {
        for (nodeId in nodesIds) {
            val channel = openChannel(nodeId)
            openStreams(channel)

            sendNumberOfObjects(data)
            sendObjectsList(data)

            closeStreams()
            closeChannel(channel)
        }
    }

    private suspend fun <T> sendNumberOfObjects(data: List<T>) {
        outputStream.writeIntSuspending(data.size)
    }

    private suspend fun <T> FlowCollector<SynchronizationStatus>.sendObjectsList(
        dataList: List<T>
    )  {
        for (data in dataList) {
            val byteArray = gson.toJson(data).toByteArray()
            outputStream.writeIntSuspending(byteArray.size)
            val synchronizationStatus = exchangeDataAsync(byteArray, data.getId())
            emit(synchronizationStatus)
        }
    }

    private suspend fun exchangeDataAsync(
        byteArray: ByteArray, id: String
    ): SynchronizationStatus {
        return try {
            outputStream.writeSuspending(byteArray)
            inputStream.readSuspending()
            SynchronizationStatus.SuccessfulSynchronization(id)
        } catch (exception: IOException) {
            Timber.w("Sending error: ${exception.localizedMessage}")
            SynchronizationStatus.FailureSynchronization(exception.toSynchronizationSendingException())
        }
    }

    private suspend fun openChannel(nodeId: String): ChannelClient.Channel =
        channelClient.openChannel(nodeId, path).await()

    private suspend fun openStreams(channel: ChannelClient.Channel) {
        outputStream = channelClient.getOutputStream(channel).await()
        inputStream = channelClient.getInputStream(channel).await()
    }

    private suspend fun closeStreams() {
        outputStream.closeSuspending()
        inputStream.closeSuspending()
    }

    private fun closeChannel(channel: ChannelClient.Channel) {
        channelClient.close(channel)
    }

    private fun <T> T.getId(): String {
        return when (this) {
            is TrainingPlanJsonModel -> id
//        is TrainingStatistics ->
            else -> throw Exception("Unknown type")
        }
    }

    private fun IOException.toSynchronizationSendingException() =
        SynchronizationSendingException(message, cause)
}
