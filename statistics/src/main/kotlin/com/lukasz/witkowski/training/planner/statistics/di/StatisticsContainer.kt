package com.lukasz.witkowski.training.planner.statistics.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.statistics.infrastructure.DbStatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDatabase
import com.lukasz.witkowski.training.planner.statistics.presentation.DefaultTimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController

class StatisticsContainer(private val context: Context) {

    private val statisticsDb = Room.databaseBuilder(
        context,
        StatisticsDatabase::class.java,
        "Statistics Database"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val statisticsRepository: StatisticsRepository by lazy {
        DbStatisticsRepository(statisticsDb.statisticsDao())
    }

    val trainingSessionService: TrainingSessionService by lazy {
        TrainingSessionService(SystemTimeProvider())
    }

    val trainingStatisticsService: TrainingStatisticsService by lazy {
        TrainingStatisticsService(statisticsRepository)
    }

    val timerController: TimerController by lazy {
        DefaultTimerController()
    }
}
