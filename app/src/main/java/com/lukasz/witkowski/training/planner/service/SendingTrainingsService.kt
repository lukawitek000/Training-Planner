package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.services.SendingDataService
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.closeSuspending
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeIntSuspending
import com.lukasz.witkowski.shared.utils.writeSuspending
import com.lukasz.witkowski.training.planner.repository.SyncDataRepository
import dagger.hilt.android.AndroidEntryPoint
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
class SendingTrainingsService : SendingDataService() {

    @Inject
    lateinit var syncDataRepository: SyncDataRepository

    override fun onCreate() {
        super.onCreate()
        observeNotSynchronizedData()
    }

    private fun observeNotSynchronizedData() {
        lifecycleScope.launch {
            syncDataRepository.getNotSynchronizedTrainings().collect {
                if (it.isNotEmpty()) {
                    Timber.d("Send trainings ${it.size} $it")
                    sendTrainings(it)
                }
            }
        }
    }

    private fun sendTrainings(trainings: List<TrainingWithExercises>) {
        lifecycleScope.launch {
            val nodeId = getConnectedNodes() ?: return@launch // TODO send to all nodes
            val channel = channelClient.openChannel(nodeId, TRAINING_PATH).await()
            val outputStream = channelClient.getOutputStream(channel).await()
            val inputStream = channelClient.getInputStream(channel).await()
            outputStream.writeIntSuspending(trainings.size)
            for (training in trainings) {
                sendSingleTraining(training, outputStream, inputStream)
            }
            outputStream.closeSuspending()
            inputStream.closeSuspending()
            channelClient.close(channel)
        }
    }

    private suspend fun sendSingleTraining(
        training: TrainingWithExercises,
        outputStream: OutputStream,
        inputStream: InputStream
    ) = withContext(Dispatchers.IO) {
        try {
            for (trainingExercise in training.exercises) {
                trainingExercise.exercise.image = null
            }
            Timber.d("Send data $training")

            val job: Deferred<Int> = async {
                try {
                    val byteArray = gson.toJson(training).toByteArray()
                    outputStream.writeSuspending(byteArray)
                    inputStream.readSuspending()
                } catch (e: Exception) {
                    Timber.d("Error during exchanging messages occurred ${e.localizedMessage}")
                    SYNC_FAILURE
                }
            }
            val syncResponse = job.await()

            Timber.d("Message returned $syncResponse")
            if (syncResponse == SYNC_SUCCESSFUL) {
                // TODO change is synchronized to true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("Saving item failed ${e.localizedMessage}")
        }
    }
}