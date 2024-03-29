package com.lukasz.witkowski.training.planner.training.infrastructure.db

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers.toDbTrainingPlanWithExercises
import com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers.toTrainingPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DbTrainingPlanRepository(
    private val trainingPlanDao: TrainingPlanDao
) : TrainingPlanRepository {

    override suspend fun save(trainingPlan: TrainingPlan) {
        val trainingPlanWithExercise = trainingPlan.toDbTrainingPlanWithExercises()
        trainingPlanDao.insertTrainingWithTrainingExercises(trainingPlanWithExercise)
    }

    override suspend fun setTrainingPlanAsSynchronized(id: TrainingPlanId) {
        // TODO Not implemented
    }

    override fun getAll(): Flow<List<TrainingPlan>> {
        return trainingPlanDao.getAll().map {
            it.map { dbTrainingPlanWithExercises ->
                dbTrainingPlanWithExercises.toTrainingPlan()
            }
        }
    }

    override suspend fun delete(trainingPlan: TrainingPlan) {
        val dbTrainingPlanWithExercises = trainingPlan.toDbTrainingPlanWithExercises()
        trainingPlanDao.deleteTrainingPlanWithExercises(dbTrainingPlanWithExercises)
    }

    override suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): TrainingPlan {
        val id = trainingPlanId.toString()
        val dbTrainingPlanWithExercises = trainingPlanDao.getTrainingPlanById(id)
        return dbTrainingPlanWithExercises.toTrainingPlan()
    }
}
