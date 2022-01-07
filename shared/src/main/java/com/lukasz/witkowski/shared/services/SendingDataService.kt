package com.lukasz.witkowski.shared.services

import androidx.lifecycle.LifecycleService
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import timber.log.Timber


open class SendingDataService : LifecycleService() {

    protected val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this) }

    protected suspend fun getConnectedNodes(): String? {
        val nodeClient = Wearable.getNodeClient(this)
        val nodes = nodeClient.connectedNodes.await()
        Timber.d("Available nodes $nodes")
        return nodes.firstOrNull()?.id
    }

}