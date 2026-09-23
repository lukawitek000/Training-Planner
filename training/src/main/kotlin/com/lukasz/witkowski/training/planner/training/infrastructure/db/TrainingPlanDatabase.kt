package com.lukasz.witkowski.training.planner.training.infrastructure.db

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlan

@Database(
    entities = [DbTrainingPlan::class, DbTrainingExercise::class],
    version = 6,
    exportSchema = false,
)
internal abstract class TrainingPlanDatabase : RoomDatabase() {
    abstract fun trainingPlanDao(): TrainingPlanDao

    companion object {
        @Volatile
        private var instance: TrainingPlanDatabase? = null

        fun getInstance(context: Context): TrainingPlanDatabase =
            synchronized(this) {
                if (instance == null) {
                    instance =
                        Room
                            .databaseBuilder(
                                context,
                                TrainingPlanDatabase::class.java,
                                "Training Plan Database",
                            ).fallbackToDestructiveMigration()
                            .build()
                }
                instance!!
            }
    }
}
