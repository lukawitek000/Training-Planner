package com.lukasz.witkowski.shared.services

import androidx.lifecycle.LifecycleService
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.repository.SyncDataRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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
        Timber.d("On create observ")
        observeNotSynchronizedData()
    }

    abstract fun observeNotSynchronizedData()

}