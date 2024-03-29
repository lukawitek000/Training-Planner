package com.lukasz.witkowski.training.planner.statistics.infrastructure

import com.lukasz.witkowski.training.planner.statistics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDao
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers.toDbTrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers.toTrainingStatistics
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

    override fun getByTrainingPlanId(trainingPlanId: TrainingPlanId): Flow<List<TrainingStatistics>> {
        return statisticsDao.getTrainingStatisticsByTrainingPlanId(trainingPlanId.toString())
            .map { statisticsList ->
                statisticsList.map { it.toTrainingStatistics() }
            }
    }

    override fun getByTrainingStatisticsId(trainingStatisticsId: TrainingStatisticsId): Flow<TrainingStatistics> {
        return statisticsDao.getTrainingStatisticsById(trainingStatisticsId.toString()).map {
            it.toTrainingStatistics()
        }
    }
}
