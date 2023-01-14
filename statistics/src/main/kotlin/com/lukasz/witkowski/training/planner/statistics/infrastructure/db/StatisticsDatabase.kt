package com.lukasz.witkowski.training.planner.statistics.infrastructure.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbTrainingStatistics

@Database(
    entities = [DbTrainingStatistics::class, DbExerciseStatistics::class, DbExerciseAttemptStatistics::class],
    version = 1,
    exportSchema = false
)
abstract class StatisticsDatabase : RoomDatabase() {
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        @Volatile
        private var instance: StatisticsDatabase? = null

        fun getInstance(context: Context): StatisticsDatabase {
            return synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        StatisticsDatabase::class.java,
                        "Statistics Database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                instance!!
            }
        }
    }
}
