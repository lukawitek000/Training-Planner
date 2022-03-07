package com.lukasz.witkowski.shared.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lukasz.witkowski.shared.models.statistics.ExerciseStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingStatistics

@Database(
    entities = [ExerciseStatistics::class, TrainingStatistics::class],
    version = 9, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statisticsDao(): StatisticsDao
}
