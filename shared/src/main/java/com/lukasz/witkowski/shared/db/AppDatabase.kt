package com.lukasz.witkowski.shared.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.statistics.ExerciseStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingStatistics

@Database(
    entities = [Training::class, TrainingExercise::class, ExerciseStatistics::class, TrainingStatistics::class],
    version = 8
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao
    abstract fun statisticsDao(): StatisticsDao
}
