package com.lukasz.witkowski.training.planner.statistics.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.statistics.infrastructure.DbStatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDao
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.StatisticsDatabase
import com.lukasz.witkowski.training.planner.statistics.presentation.DefaultTimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object StatisticsModule {

    @Provides
    fun provideTrainingSessionService(): TrainingSessionService {
        return TrainingSessionService(SystemTimeProvider())
    }

    @Provides
    fun provideTimerController(): TimerController {
        return DefaultTimerController()
    }

    @Provides
    fun provideStatisticsDatabase(@ApplicationContext context: Context): StatisticsDatabase {
        return Room.databaseBuilder(
            context,
            StatisticsDatabase::class.java,
            "Statistics Database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStatisticsDao(statisticsDatabase: StatisticsDatabase): StatisticsDao {
        return statisticsDatabase.statisticsDao()
    }

    @Provides
    fun provideStatisticsRepository(statisticsDao: StatisticsDao): DbStatisticsRepository {
        return DbStatisticsRepository(statisticsDao)
    }

    @Provides
    fun provideTrainingStatisticsService(statisticsRepository: DbStatisticsRepository): TrainingStatisticsService {
        return TrainingStatisticsService(statisticsRepository)
    }
}