package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.service.SendingDataService
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import com.lukasz.witkowski.shared.utils.TRAINING_PATH
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SendingTrainingsService : SendingDataService() {

    override fun observeNotSynchronizedData() {
//        lifecycleScope.launch {
//            syncDataRepository.getNotSynchronizedTrainings().collect {
//                if (it.isNotEmpty()) {
//                    removeImages(it)
//                    Timber.d("Send trainings ${it.size} $it")
//                    sendData(data = it, path = TRAINING_PATH)
//                }
//            }
//        }
    }

//    private fun removeImages(it: List<TrainingWithExercises>) {
//        it.forEach { trainingWithExercises ->
//            trainingWithExercises.exercises.forEach { trainingExercise ->
//                trainingExercise.exercise.image = null
//            }
//        }
//    }

    override suspend fun handleSyncResponse(id: Long, syncResponse: Int) {
        Timber.d("Sending training $id response $syncResponse")
//        if(syncResponse == SYNC_SUCCESSFUL) {
//            syncDataRepository.updateSynchronizedTraining(id)
//        } else {
//            Timber.w("Sending training $id failed")
//        }
    }
}
