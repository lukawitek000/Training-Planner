package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.exercise.domain.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class TrainingPlanService(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val sendTrainingPlanRepository: TrainingPlanSender
){

    suspend fun saveTrainingPlan(trainingPlan: TrainingPlan) {
        trainingPlanRepository.save(trainingPlan)
        sendData(listOf(trainingPlan))
    }

    fun getAllTrainingPlans(categories: List<Category>): Flow<List<TrainingPlan>> {
        return trainingPlanRepository.getAll().map {
            it.filter { trainingPlan -> categories.isEmpty() || hasTrainingPlanCategories(trainingPlan, categories) }
        }
    }

    // TODO it can applied in domain data class (getter for list of categories)
    private fun hasTrainingPlanCategories(trainingPlan: TrainingPlan, categories: List<Category>): Boolean {
        return trainingPlan.exercises.map{ exercise -> exercise.category }.containsAll(categories.map { it.name })
    }

    suspend fun sendNotSynchronizedTrainingPlans() {
        trainingPlanRepository.getAll().map { it.filter { trainingPlan -> !trainingPlan.isSynchronized } }.collect {
            sendData(it)
        }
    }

    private suspend fun sendData(trainingPlans: List<TrainingPlan>) {
        sendTrainingPlanRepository.send(trainingPlans).collect {id ->
            trainingPlanRepository.setTrainingPlanAsSynchronized(id)
            Timber.d("Sent data id $id")
        }
    }
}