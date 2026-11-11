package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics

fun interface SessionStatisticsProvider {
    fun getStatistics(): TrainingStatistics
}
