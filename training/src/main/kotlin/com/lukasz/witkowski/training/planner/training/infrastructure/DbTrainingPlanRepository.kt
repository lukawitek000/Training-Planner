package com.lukasz.witkowski.training.planner.training.infrastructure

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.mappers.toDbTrainingPlanWithExercises
import com.lukasz.witkowski.training.planner.training.infrastructure.mappers.toTrainingPlan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

internal class DbTrainingPlanRepository(
    private val trainingPlanDao: TrainingPlanDao,
    private val dispatcher: CoroutineDispatcher,
) : TrainingPlanRepository {
    override suspend fun save(trainingPlan: TrainingPlan): Result<TrainingPlanId> {
        val trainingPlanWithExercise = trainingPlan.toDbTrainingPlanWithExercises()
        trainingPlanDao.insertTrainingWithTrainingExercises(trainingPlanWithExercise)
    }

    override fun getAll(): Flow<List<TrainingPlanOverview>> =
        trainingPlanDao.getAll().map {
            it.map { dbTrainingPlanWithExercises ->
                dbTrainingPlanWithExercises.toTrainingPlan()
            }
        }

    override suspend fun delete(trainingPlanId: TrainingPlanId): Result<Unit> {
        TODO("Not yet implemented")
    }

//    override suspend fun delete(trainingPlan: TrainingPlan) {
//        val dbTrainingPlanWithExercises = trainingPlan.toDbTrainingPlanWithExercises()
//        trainingPlanDao.deleteTrainingPlanWithExercises(dbTrainingPlanWithExercises)
//    }

    override suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): Flow<TrainingPlan> {
        val id = trainingPlanId.toString()
        val dbTrainingPlanWithExercises = trainingPlanDao.getTrainingPlanById(id)
        return dbTrainingPlanWithExercises.toTrainingPlan()
    }

    override suspend fun update(trainingPlan: TrainingPlan): Result<TrainingPlanId> {
        TODO("Not yet implemented")
    }
}
