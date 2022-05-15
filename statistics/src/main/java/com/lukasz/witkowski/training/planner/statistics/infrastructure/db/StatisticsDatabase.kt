package com.lukasz.witkowski.training.planner.statistics.infrastructure.db

import androidx.room.Database
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
}
