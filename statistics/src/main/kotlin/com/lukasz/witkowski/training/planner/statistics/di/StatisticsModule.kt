package com.lukasz.witkowski.training.planner.statistics.di

import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.timer.Timer
import com.lukasz.witkowski.training.planner.statistics.infrastructure.DbStatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val statisticsModule = module {
    includes(dispatchersModule)
    single { StatisticsDatabase.getInstance(androidContext()) }
    single { get<StatisticsDatabase>().statisticsDao() }

    single<DbStatisticsRepository>() bind StatisticsRepository::class
    single<TrainingStatisticsService>()
    single {
        TrainingSessionService(
            timeProvider = SystemTimeProvider(),
            timer = Timer(),
            trainingStatisticsService = get(),
            trainingSetsStrategy = CircuitSetsPolicy(),
            backgroundDispatcher = get(named(CoroutineDispatcherQualifiers.DEFAULT))
        )

    }
}