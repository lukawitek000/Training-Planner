package com.lukasz.witkowski.training.planner.training.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DbTrainingPlan::class, DbExercise::class], version = 0, exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class TrainingPlanDatabase : RoomDatabase() {
    abstract fun trainingPlanDao(): TrainingPlanDao
}