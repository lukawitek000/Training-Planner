package com.lukasz.witkowski.training.planner.analytics.infrastructure

import com.lukasz.witkowski.training.planner.analytics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId

internal class AnalyticsSessionCompletedListener(
    private val statisticsRepository: StatisticsRepository
) : SessionCompletedListener {
    override suspend fun onSessionCompleted(statistics: TrainingStatistics) {
        statisticsRepository.save(statistics)
    }

    override suspend fun onSessionMetadataUpdated(
        id: TrainingStatisticsId,
        rpe: SessionRpe?,
        note: String?
    ) {
        statisticsRepository.updateMetadata(id, rpe, note)
    }
}
