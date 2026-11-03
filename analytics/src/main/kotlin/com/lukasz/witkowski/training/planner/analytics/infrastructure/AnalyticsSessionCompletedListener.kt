package com.lukasz.witkowski.training.planner.analytics.infrastructure

import com.lukasz.witkowski.training.planner.analytics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics

internal class AnalyticsSessionCompletedListener(
    private val statisticsRepository: StatisticsRepository
) : SessionCompletedListener {
    override suspend fun onSessionCompleted(statistics: TrainingStatistics) {
        statisticsRepository.save(statistics)
    }
}
