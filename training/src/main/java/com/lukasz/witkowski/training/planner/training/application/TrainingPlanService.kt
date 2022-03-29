package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.exercise.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanReceiver
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
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
        sendData(listOf(trainingPlan))
    }

    fun getAllTrainingPlans(categories: List<com.lukasz.witkowski.training.planner.exercise.Category> = emptyList()): Flow<List<TrainingPlan>> {
        return trainingPlanRepository.getAll().map {
            it.filter { trainingPlan -> categories.isEmpty() || trainingPlan.hasCategories(categories) }
        }
    }

    suspend fun sendNotSynchronizedTrainingPlans() {
        trainingPlanRepository.getAll().map { it.filter { trainingPlan -> !trainingPlan.isSynchronized } }.collect {
            sendData(it)
        }
    }

    private suspend fun sendData(trainingPlans: List<TrainingPlan>) {
        trainingPlanSender.send(trainingPlans).collect {id ->
            trainingPlanRepository.setTrainingPlanAsSynchronized(id)
            Timber.d("Sent data id $id")
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun receiveTrainingPlan(inputStream: InputStream, outputStream: OutputStream) {
        coroutineScope.launch {
            trainingPlanReceiver.receiveTrainingPlan(inputStream, outputStream).collect {
                trainingPlanRepository.save(it)
            }
        }
    }
}