package com.lukasz.witkowski.shared.services

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.repository.SyncDataRepository
import com.lukasz.witkowski.shared.utils.STATISTICS_PATH
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@AndroidEntryPoint
abstract class SendingDataService : LifecycleService() {

    @Inject
    lateinit var syncDataRepository: SyncDataRepository
    protected val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }

    protected suspend fun getConnectedNodes(): String? {
        val nodeClient = Wearable.getNodeClient(this)
        val nodes = nodeClient.connectedNodes.await()
        Timber.d("Available nodes $nodes")
        return nodes.firstOrNull()?.id
    }

    override fun onCreate() {
        super.onCreate()
        observeNotSynchronizedData()
    }

    protected fun sendData(dataListSize: Int, sendSingleData: suspend (OutputStream, InputStream) -> Unit) {
        lifecycleScope.launch {
            val nodeId = getConnectedNodes() ?: return@launch // TODO send to all nodes
            val channel = channelClient.openChannel(nodeId, STATISTICS_PATH).await()
            val outputStream = channelClient.getOutputStream(channel).await()
            val inputStream = channelClient.getInputStream(channel).await()
            outputStream.writeIntSuspending(dataListSize)
            sendSingleData.invoke(outputStream, inputStream)
            outputStream.closeSuspending()
            inputStream.closeSuspending()
            channelClient.close(channel)
        }
    }

    abstract fun observeNotSynchronizedData()

}