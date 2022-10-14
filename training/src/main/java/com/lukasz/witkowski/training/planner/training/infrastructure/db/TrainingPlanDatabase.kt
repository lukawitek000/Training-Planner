package com.lukasz.witkowski.training.planner.training.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlan

@Database(
    entities = [DbTrainingPlan::class, DbTrainingExercise::class], version = 6, exportSchema = false
)
internal abstract class TrainingPlanDatabase : RoomDatabase() {
    abstract fun trainingPlanDao(): TrainingPlanDao
}
