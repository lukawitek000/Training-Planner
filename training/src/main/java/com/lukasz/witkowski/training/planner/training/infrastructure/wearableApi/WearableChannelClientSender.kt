package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import android.content.Context
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

class WearableChannelClientSender(
    private val context: Context,
    private val path: String
    ) {


    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }

    fun <T, K> sendData(data: List<T>): Flow<K> = flow {
        coroutineScope.launch {
            val nodesIds = getConnectedNodesIds()
            sendDataToEachNode(nodesIds, data)
        }
    }

    private suspend fun getConnectedNodesIds(): List<String> {
        val nodeClient = Wearable.getNodeClient(context)
        val nodes = nodeClient.connectedNodes.await()
        return nodes.map { it.id }
    }

    private suspend fun <T, K> FlowCollector<K>.sendDataToEachNode(
        nodesIds: List<String>,
        data: List<T>
    ) {
        for (nodeId in nodesIds) {
            val channel = openChannel(nodeId)
            val (outputStream, inputStream) = openStreams(channel)

            sendNumberOfObjects(outputStream, data)
            sendObjectsList(data, outputStream, inputStream)

            closeStreams(outputStream, inputStream)
            closeChannel(channel)
        }
    }

    private suspend fun openChannel(nodeId: String): ChannelClient.Channel =
        channelClient.openChannel(nodeId, path).await()

    private suspend fun openStreams(channel: ChannelClient.Channel): Pair<OutputStream, InputStream> {
        val outputStream = channelClient.getOutputStream(channel).await()
        val inputStream = channelClient.getInputStream(channel).await()
        return Pair(outputStream, inputStream)
    }

    private suspend fun closeStreams(
        outputStream: OutputStream,
        inputStream: InputStream
    ) {
        outputStream.closeSuspending()
        inputStream.closeSuspending()
    }

    private fun closeChannel(channel: ChannelClient.Channel) {
        channelClient.close(channel)
    }

    private suspend fun <T> sendNumberOfObjects(
        outputStream: OutputStream,
        data: List<T>
    ) {
        outputStream.writeIntSuspending(data.size)
    }

    private suspend fun <K, T> FlowCollector<K>.sendObjectsList(
        data: List<T>,
        outputStream: OutputStream?,
        inputStream: InputStream?
    ) {
        for (item in data) {
            val synchronizedId = sendSingleData(item)
            emit(synchronizedId)
        }
    }

    private suspend fun <K, T> sendSingleData(
        data: T
    ): K = withContext(Dispatchers.IO) {
        try {
            val byteArray = gson.toJson(data).toByteArray()
            outputStream.writeIntSuspending(byteArray.size)
            val job = exchangeDataAsync(outputStream, byteArray, inputStream)
            val id = getDataId(data)
            "" as K
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("Saving item failed ${e.localizedMessage}")
            WearableChannelClientTrainingPlanSender.SYNCHRONIZATION_FAILED
            "" as K
        }
    }


}