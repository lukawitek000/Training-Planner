package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Tasks.await
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.TRAINING_KEY
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.training.planner.repository.SyncDataRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SendingDataService : LifecycleService() {

    @Inject
    lateinit var syncDataRepository: SyncDataRepository

    private val dataClient: DataClient by lazy { Wearable.getDataClient(this) }

    override fun onCreate() {
        super.onCreate()
        observeNotSynchronizedData()
    }

    private fun observeNotSynchronizedData() {
        syncDataRepository.getNotSynchronizedTrainings().observe(this) {
            for(training in it) {
                sendTraining(training)
            }
        }
    }

    private fun sendTraining(training: TrainingWithExercises) {
        lifecycleScope.launch {
            try {
                val data = training.toAsset()

                val putDataRequest = PutDataMapRequest.create(TRAINING_PATH).apply {
                    dataMap.putAsset(TRAINING_KEY, data)
                }
                    .asPutDataRequest()
                val result = dataClient.putDataItem(putDataRequest)
            } catch (cancellationException: CancellationException) {
                Timber.d("Job has been cancelled")
            } catch (e: Exception) {
                Timber.d("Saving item failes")
            }
        }

    }

    private suspend fun TrainingWithExercises.toAsset(): Asset =
        withContext(Dispatchers.Default) {
            val byteArray = Gson().toJson(training).toByteArray()
            Asset.createFromBytes(byteArray)
        }

}