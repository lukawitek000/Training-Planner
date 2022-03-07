package com.lukasz.witkowski.training.planner.training.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlan

@Database(
    entities = [DbTrainingPlan::class, DbExercise::class], version = 2, exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class TrainingPlanDatabase : RoomDatabase() {
    abstract fun trainingPlanDao(): TrainingPlanDao
}