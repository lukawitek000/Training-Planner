package com.lukasz.witkowski.training.planner.training.infrastructure

import com.lukasz.witkowski.training.planner.shared.time.TimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingQuery
import com.lukasz.witkowski.training.planner.training.infrastructure.mappers.toDbTrainingPlanWithExercises
import com.lukasz.witkowski.training.planner.training.infrastructure.mappers.toTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.mappers.toTrainingPlanOverview
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.collections.map
import kotlin.time.Clock

internal class DbTrainingPlanRepository(
    private val trainingPlanDao: TrainingPlanDao,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
) : TrainingPlanRepository {
    override suspend fun save(
        trainingPlanConfiguration: TrainingPlanConfiguration,
        id: TrainingPlanId
    ): Result<TrainingPlanId> = withContext(dispatcher) {
        runCatchingCancellable {
            val trainingPlanWithExercise = trainingPlanConfiguration.toDbTrainingPlanWithExercises(
                id = id,
                currentInstant = timeProvider.currentInstant()
            )
            trainingPlanDao.insertTrainingWithTrainingExercises(trainingPlanWithExercise)
            id
        }
    }

    override fun getAll(trainingQuery: TrainingQuery): Flow<List<TrainingPlanOverview>> =
        trainingPlanDao.getAllTrainingOverviews().map { list ->
            list.map { it.toTrainingPlanOverview() }
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
        return trainingPlanDao.getTrainingPlanById(id).map {
            it.toTrainingPlan()
        }
    }

    override suspend fun update(
        trainingPlanConfiguration: TrainingPlanConfiguration,
        id: TrainingPlanId
    ): Result<TrainingPlanId> {
        TODO("Not yet implemented")
    }
}
