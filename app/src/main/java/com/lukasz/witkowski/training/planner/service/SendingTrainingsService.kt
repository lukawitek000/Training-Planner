package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.services.SendingDataService
import com.lukasz.witkowski.shared.utils.STATISTICS_PATH
import com.lukasz.witkowski.shared.utils.SYNC_FAILURE
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import com.lukasz.witkowski.shared.utils.gson
import com.lukasz.witkowski.shared.utils.readSuspending
import com.lukasz.witkowski.shared.utils.writeSuspending
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

@AndroidEntryPoint
class SendingTrainingsService : SendingDataService() {

    override fun observeNotSynchronizedData() {
        lifecycleScope.launch {
            syncDataRepository.getNotSynchronizedTrainings().collect {
                if (it.isNotEmpty()) {
                    removeImages(it)
                    Timber.d("Send trainings ${it.size} $it")
                    sendData(data = it, path = TRAINING_PATH)
                }
            }
        }
    }

    private fun removeImages(it: List<TrainingWithExercises>) {
        it.forEach { trainingWithExercises ->
            trainingWithExercises.exercises.forEach { trainingExercise ->
                trainingExercise.exercise.image = null
            }
        }
    }

    override suspend fun handleSyncResponse(id: Long, syncResponse: Int) {
        Timber.d("Sending training $id response $syncResponse")
        if(syncResponse == SYNC_SUCCESSFUL) {
            syncDataRepository.updateSynchronizedTraining(id)
        } else {
            Timber.w("Sending training $id failed")
        }
    }
}
