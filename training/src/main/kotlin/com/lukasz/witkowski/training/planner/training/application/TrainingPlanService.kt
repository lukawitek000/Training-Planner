package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.synchronization.SynchronizationStatus
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

class TrainingPlanService(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingPlanSender: TrainingPlanSender
) {

    suspend fun saveTrainingPlan(trainingPlan: TrainingPlan) {
        trainingPlanRepository.save(trainingPlan)
    }

    fun getTrainingPlansFromCategories(categories: List<ExerciseCategory> = emptyList()): Flow<List<TrainingPlan>> {
        return trainingPlanRepository.getAll().map {
            it.filter { trainingPlan ->
                categories.isEmpty() || trainingPlan.hasCategories(
                    categories
                )
            }
        }
    }

    suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): TrainingPlan {
        return trainingPlanRepository.getTrainingPlanById(trainingPlanId)
    }

    suspend fun sendTrainingPlan(trainingPlan: TrainingPlan) {
        sendData(listOf(trainingPlan))
    }

    private suspend fun sendData(trainingPlans: List<TrainingPlan>) {
        try {
            trainingPlanSender.send(trainingPlans).collect {
                Timber.d("Send Training Plans $it")
                if (it is SynchronizationStatus.Successful) {
                    // TODO handle successful synchronization
                } else {
                    // TODO handle failed synchronization
                }
            }
        } catch (e: IOException) {
            Timber.d("Sending failed ${e.message}")
        }
    }
}
