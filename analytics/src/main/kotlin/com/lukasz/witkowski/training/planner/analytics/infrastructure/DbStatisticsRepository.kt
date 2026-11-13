package com.lukasz.witkowski.training.planner.analytics.infrastructure

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.lukasz.witkowski.training.planner.analytics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.analytics.domain.models.TrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.StatisticsDao
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers.toDbTrainingStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers.toTrainingStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers.toTrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DbStatisticsRepository(
    private val statisticsDao: StatisticsDao,
    private val pagingConfig: PagingConfig = PagingConfig(pageSize = 20, enablePlaceholders = false)
) : StatisticsRepository {
    override suspend fun save(trainingStatistics: TrainingStatistics) {
        val dbTrainingStatistics = trainingStatistics.toDbTrainingStatistics()
        statisticsDao.insertAllStatistics(dbTrainingStatistics)
    }

    override suspend fun updateMetadata(id: TrainingStatisticsId, rpe: SessionRpe?, note: String?) {
        statisticsDao.updateMetadata(id.toString(), rpe?.score, note)
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

    override fun getOverviewsByTrainingPlanId(
        trainingPlanId: TrainingPlanId
    ): Flow<PagingData<TrainingStatisticsOverview>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = {
            statisticsDao.getTrainingStatisticsOverviewsByTrainingPlanId(trainingPlanId.toString())
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toTrainingStatisticsOverview() }
    }

    override fun getByTrainingStatisticsId(
        trainingStatisticsId: TrainingStatisticsId
    ): Flow<TrainingStatistics?> =
        statisticsDao.getTrainingStatisticsById(trainingStatisticsId.toString()).map {
            it?.toTrainingStatistics()
        }
}
