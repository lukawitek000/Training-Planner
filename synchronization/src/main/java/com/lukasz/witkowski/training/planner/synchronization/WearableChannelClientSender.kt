package com.lukasz.witkowski.training.planner.synchronization

import android.content.Context
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.training.planner.synchronization.WearableChannelClientReceiver.Companion.ACKNOWLEDGE_FLAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * T is a type of objects that are sent
 * K is a type of their id (from domain)
 */
class WearableChannelClientSender<T, K>(
    private val context: Context,
    private val path: String,
    private val getId: T.() -> K
) {

    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    /**
     * If sending data has failed the [SynchronizationStatus.Failure] is emitted,
     * otherwise the [SynchronizationStatus.Successful] with id of the successfully synchronized object
     */
    fun sendData(data: List<T>): Flow<SynchronizationStatus<K>> = flow {
        val nodesIds = getConnectedNodesIds()
        sendDataToEachNode(nodesIds, data)
    }

    private suspend fun getConnectedNodesIds(): List<String> {
        val nodeClient = Wearable.getNodeClient(context)
        val nodes = nodeClient.connectedNodes.await()
        return nodes.map { it.id }
    }

    private suspend fun FlowCollector<SynchronizationStatus<K>>.sendDataToEachNode(
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

    private suspend fun sendNumberOfObjects(data: List<T>) {
        outputStream.writeIntSuspending(data.size)
    }

    private suspend fun FlowCollector<SynchronizationStatus<K>>.sendObjectsList(
        dataList: List<T>
    ) {
        for (data in dataList) {
            val json = gson.toJson(data)
            val byteArray = json.toByteArray()
            sendBufferSize(byteArray)
            val synchronizationStatus =
                sendObjectAndWaitForAcknowledge(byteArray, getId.invoke(data))
            emit(synchronizationStatus)
        }
    }

    private suspend fun sendBufferSize(byteArray: ByteArray) {
        val bufferSize = byteArray.size.toByteArray()
        outputStream.writeSuspending(bufferSize)
    }

    private suspend fun sendObjectAndWaitForAcknowledge(
        byteArray: ByteArray, id: K
    ): SynchronizationStatus<K> {
        return try {
            outputStream.writeSuspending(byteArray)
            receiveAcknowledge(id)
        } catch (exception: IOException) {
            Timber.w("Sending error: ${exception.localizedMessage}")
            SynchronizationStatus.Failure(id, exception.toSynchronizationSendingException())
        } catch (exception: SynchronizationSavingException) {
            Timber.w("Sending error: ${exception.message}")
            SynchronizationStatus.Failure(id, exception)
        }
    }

    private suspend fun receiveAcknowledge(id: K): SynchronizationStatus.Successful<K> {
        val response = inputStream.readSuspending()
        return if (response == ACKNOWLEDGE_FLAG) {
            SynchronizationStatus.Successful(id)
        } else {
            throw SynchronizationSavingException(
                message = "Peer has failed to save data"
            )
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

    private fun IOException.toSynchronizationSendingException() =
        SynchronizationSendingException(
            message,
            cause
        )
}
