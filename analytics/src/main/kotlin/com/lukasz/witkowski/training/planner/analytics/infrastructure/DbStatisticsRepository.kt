package com.lukasz.witkowski.training.planner.analytics.infrastructure

import com.lukasz.witkowski.training.planner.analytics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.StatisticsDao
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers.toDbTrainingStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers.toTrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DbStatisticsRepository(private val statisticsDao: StatisticsDao) : StatisticsRepository {
    override suspend fun save(trainingStatistics: TrainingStatistics) {
        val dbTrainingStatistics = trainingStatistics.toDbTrainingStatistics()
        statisticsDao.insertAllStatistics(dbTrainingStatistics)
    }

    override fun delete(trainingStatistics: TrainingStatistics) {
//        TODO("Not yet implemented")
    }

    override fun getByTrainingPlanId(
        trainingPlanId: TrainingPlanId
    ): Flow<List<TrainingStatistics>> = statisticsDao
        .getTrainingStatisticsByTrainingPlanId(trainingPlanId.toString())
        .map { statisticsList ->
            statisticsList.map { it.toTrainingStatistics() }
        }

    override fun getByTrainingStatisticsId(
        trainingStatisticsId: TrainingStatisticsId
    ): Flow<TrainingStatistics> =
        statisticsDao.getTrainingStatisticsById(trainingStatisticsId.toString()).map {
            it.toTrainingStatistics()
        }
}
