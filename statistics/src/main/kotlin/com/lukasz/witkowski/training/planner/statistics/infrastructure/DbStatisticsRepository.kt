package com.lukasz.witkowski.training.planner.statistics.infrastructure

import com.lukasz.witkowski.training.planner.statistics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDao
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers.TrainingStatisticsMapper
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DbStatisticsRepository(private val statisticsDao: StatisticsDao) : StatisticsRepository {
    override suspend fun save(trainingStatistics: TrainingStatistics) {
        val dbTrainingStatistics =
            TrainingStatisticsMapper.toDbTrainingStatistics(trainingStatistics)
        statisticsDao.insertAllStatistics(dbTrainingStatistics)
    }

    override fun delete(trainingStatistics: TrainingStatistics) {
//        TODO("Not yet implemented")
    }

    override fun getByTrainingPlanId(trainingPlanId: TrainingPlanId): Flow<List<TrainingStatistics>> {
        return statisticsDao.getTrainingStatistics(trainingPlanId.toString()).map { statisticsList ->
            statisticsList.map { TrainingStatisticsMapper.toTrainingStatistics(it) }
        }
    }
}
