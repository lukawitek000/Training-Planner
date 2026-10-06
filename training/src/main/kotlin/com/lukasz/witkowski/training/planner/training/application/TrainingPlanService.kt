package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrainingPlanService(
    private val trainingPlanRepository: TrainingPlanRepository,
) {
    fun getTrainingPlansOverviews(query: TrainingQuery) = trainingPlanRepository.getAll(query)

    suspend fun saveTrainingPlan(trainingPlan: TrainingPlan) {
//        trainingPlanRepository.save(trainingPlan)
    }

    fun getTrainingPlansFromCategories(categories: List<ExerciseCategoryName> = emptyList()): Flow<List<TrainingPlan>> = flow { }

    fun getTrainingPlanById(trainingPlanId: TrainingPlanId): Flow<TrainingPlan?> =
        trainingPlanRepository.getTrainingPlanById(trainingPlanId)
}
