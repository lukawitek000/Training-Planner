package com.lukasz.witkowski.training.planner.analytics.di

import com.lukasz.witkowski.training.planner.analytics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.analytics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.analytics.infrastructure.AnalyticsSessionCompletedListener
import com.lukasz.witkowski.training.planner.analytics.infrastructure.DbStatisticsRepository
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.StatisticsDatabase
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val analyticsModule = module {
    single { StatisticsDatabase.getInstance(androidContext()) }
    single { get<StatisticsDatabase>().statisticsDao() }

    single<StatisticsRepository> { DbStatisticsRepository(statisticsDao = get()) }
    single { TrainingStatisticsService(statisticsRepository = get()) }

    single<SessionCompletedListener> {
        AnalyticsSessionCompletedListener(statisticsRepository = get())
    }
}
