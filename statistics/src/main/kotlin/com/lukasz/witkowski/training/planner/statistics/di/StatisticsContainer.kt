package com.lukasz.witkowski.training.planner.statistics.di

import android.content.Context
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.statistics.infrastructure.DbStatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDatabase
import com.lukasz.witkowski.training.planner.statistics.presentation.CoroutinesTimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.training.di.TrainingContainer

class StatisticsContainer private constructor(context: Context) {

    private val statisticsRepository: StatisticsRepository by lazy {
        val statisticsDb = StatisticsDatabase.getInstance(context)
        DbStatisticsRepository(statisticsDb.statisticsDao())
    }

    val trainingSessionService: TrainingSessionService by lazy {
        TrainingSessionService(SystemTimeProvider())
    }

    val trainingStatisticsService: TrainingStatisticsService by lazy {
        TrainingStatisticsService(statisticsRepository)
    }

    fun timerController(): TimerController {
        return CoroutinesTimerController()
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
