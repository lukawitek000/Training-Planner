package com.lukasz.witkowski.training.planner.training.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlan

@Database(
    entities = [DbTrainingPlan::class, DbExercise::class], version = 5, exportSchema = false
)
internal abstract class TrainingPlanDatabase : RoomDatabase() {
    abstract fun trainingPlanDao(): TrainingPlanDao
}
