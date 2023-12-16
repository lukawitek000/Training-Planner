package com.lukasz.witkowski.training.planner.statistics.di

import android.content.Context
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.timer.Timer
import com.lukasz.witkowski.training.planner.statistics.infrastructure.DbStatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDatabase

class StatisticsContainer private constructor(context: Context) {

    private val statisticsRepository: StatisticsRepository by lazy {
        val statisticsDb = StatisticsDatabase.getInstance(context)
        DbStatisticsRepository(statisticsDb.statisticsDao())
    }

    val trainingSessionService: TrainingSessionService by lazy {
        TrainingSessionService(SystemTimeProvider(), Timer(), trainingStatisticsService)
    }

    val trainingStatisticsService: TrainingStatisticsService by lazy {
        TrainingStatisticsService(statisticsRepository)
    }

    companion object {
        @Volatile
        private var instance: StatisticsContainer? = null

        fun getInstance(context: Context): StatisticsContainer {
            return synchronized(this) {
                if (instance == null) {
                    instance = StatisticsContainer(context)
                }
                instance!!
            }
        }
    }
}
