package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.synchronization.SynchronizationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class TrainingPlanService(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingPlanSender: TrainingPlanSender
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
            if (it is SynchronizationStatus.Successful) {
                Timber.d("Successfully synchronized")
//                trainingPlanRepository.setTrainingPlanAsSynchronized(it.id) // TODO uncomment to set synchronized to database
            } else {
                // TODO handle failed synchronization
            }
        }
    }
}
