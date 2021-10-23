package com.lukasz.witkowski.shared.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.shared.db.AppDatabase
import com.lukasz.witkowski.shared.db.ExerciseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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

    @Provides
    fun provideExerciseDao(appDatabase: AppDatabase): ExerciseDao {
        return appDatabase.exerciseDao()
    }

}