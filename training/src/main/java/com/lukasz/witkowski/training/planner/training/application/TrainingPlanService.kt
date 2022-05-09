package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanReceiver
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.SynchronizationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

class TrainingPlanService(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingPlanSender: TrainingPlanSender,
    private val trainingPlanReceiver: TrainingPlanReceiver
){

    suspend fun saveTrainingPlan(trainingPlan: TrainingPlan) {
        trainingPlanRepository.save(trainingPlan)
//        sendData(listOf(trainingPlan))
    }

    fun getTrainingPlansFromCategories(categories: List<ExerciseCategory> = emptyList()): Flow<List<TrainingPlan>> {
        return trainingPlanRepository.getAll().map {
            it.filter { trainingPlan -> categories.isEmpty() || trainingPlan.hasCategories(categories) }
        }
    }

    suspend fun sendNotSynchronizedTrainingPlans() {
        trainingPlanRepository.getAll().map { it.filter { trainingPlan -> !trainingPlan.isSynchronized } }.collect {
//            sendData(it)
        }
    }

    suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): TrainingPlan {
        return trainingPlanRepository.getTrainingPlanById(trainingPlanId)
    }

    suspend fun sendTrainingPlan(trainingPlan: TrainingPlan) {
        sendData(listOf(trainingPlan))
    }

    private suspend fun sendData(trainingPlans: List<TrainingPlan>) {
        trainingPlanSender.send(trainingPlans).collect {
            Timber.d("Send Training Plans $it")
            if (it is SynchronizationStatus.Successful<*>) {
                Timber.d("Successfully synchronized")
//                trainingPlanRepository.setTrainingPlanAsSynchronized(it.id as TrainingPlanId) // TODO cannot cast string to TrainingPlanId
            } else {
                // TODO handle failed synchronization
            }
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun receiveTrainingPlan(inputStream: InputStream, outputStream: OutputStream) {
        coroutineScope.launch {
            trainingPlanReceiver.receiveTrainingPlan(inputStream, outputStream).collect {
                trainingPlanRepository.save(it)
                trainingPlanReceiver.confirmReceivingTrainingPlan(it.id)
            }
        }
    }
}