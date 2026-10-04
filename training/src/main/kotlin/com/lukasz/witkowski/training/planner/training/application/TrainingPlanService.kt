package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrainingPlanService(
    private val trainingPlanRepository: TrainingPlanRepository,
) {
    suspend fun saveTrainingPlan(trainingPlan: TrainingPlan) {
        trainingPlanRepository.save(trainingPlan)
    }

    fun getTrainingPlansFromCategories(categories: List<ExerciseCategoryName> = emptyList()): Flow<List<TrainingPlan>> =
        trainingPlanRepository.getAll().map {
            it.filter { trainingPlan ->
                categories.isEmpty() ||
                    trainingPlan.hasCategories(
                        categories,
                    )
            }
        }

    suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): TrainingPlan =
        trainingPlanRepository.getTrainingPlanById(trainingPlanId)
}
