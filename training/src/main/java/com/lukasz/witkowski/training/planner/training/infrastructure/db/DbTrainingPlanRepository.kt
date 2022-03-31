package com.lukasz.witkowski.training.planner.training.infrastructure.db

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers.TrainingPlanMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DbTrainingPlanRepository(
    private val trainingPlanDao: TrainingPlanDao
) : TrainingPlanRepository {

    override suspend fun save(trainingPlan: TrainingPlan) {
        val trainingPlanWithExercise =
            TrainingPlanMapper.toDbTrainingPlanWithExercises(trainingPlan)
        trainingPlanDao.insertTrainingWithTrainingExercises(trainingPlanWithExercise)
    }

    override suspend fun setTrainingPlanAsSynchronized(id: String) {
        // TODO Set training plan as synchronized
    }

    override fun getAll(): Flow<List<TrainingPlan>> {
        return trainingPlanDao.getAll().map {
            it.map { dbTrainingPlanWithExercises ->
                TrainingPlanMapper.toTrainingPlan(dbTrainingPlanWithExercises)
            }
        }
    }

    override suspend fun delete(trainingPlan: TrainingPlan) {
        val dbTrainingPlanWithExercises = TrainingPlanMapper.toDbTrainingPlanWithExercises(trainingPlan)
        trainingPlanDao.deleteTrainingPlanWithExercises(dbTrainingPlanWithExercises)
    }
}