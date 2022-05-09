package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import android.content.Context
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers.TrainingPlanMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class WearableTrainingPlanSender(private val context: Context): TrainingPlanSender {

    companion object {
        private const val SYNCHRONIZATION_FAILED = "Synchronization failed"
    }

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }
    private val sender = WearableChannelClientSender(context, TRAINING_PATH)

    override fun send(trainingPlans: List<TrainingPlan>): Flow<SynchronizationStatus> {
        Timber.d("Send data")
        val trainingPlansJson = trainingPlans.map { TrainingPlanMapper.toTrainingPlanJsonModel(it) }
        return sender.sendData(trainingPlansJson)
    }

    suspend fun handleSyncResponse(id: Long, syncResponse: Int): String {
        Timber.d("Sending training $id response $syncResponse")
        if(syncResponse == SYNC_SUCCESSFUL) {
//            syncDataRepository.updateSynchronizedTraining(id) // TODO mark data synchronization ??
            return id.toString()
        }
        throw Exception(SYNCHRONIZATION_FAILED)
    }
}