package com.lukasz.witkowski.training.planner.training.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DbTrainingPlan::class, DbExercise::class], version = 0, exportSchema = false
)
internal abstract class TrainingPlanDatabase : RoomDatabase() {
    abstract fun trainingPlanDao(): TrainingPlanDao
}