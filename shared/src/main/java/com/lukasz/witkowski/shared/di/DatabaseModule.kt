package com.lukasz.witkowski.shared.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.shared.db.AppDatabase
import com.lukasz.witkowski.shared.db.StatisticsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "Training Planner Database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideStatisticsDao(appDatabase: AppDatabase): StatisticsDao {
        return appDatabase.statisticsDao()
    }
}