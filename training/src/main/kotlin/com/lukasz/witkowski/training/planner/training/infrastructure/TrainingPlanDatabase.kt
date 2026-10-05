package com.lukasz.witkowski.training.planner.training.infrastructure

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbExerciseCategory
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlan

@Database(
    entities = [DbTrainingPlan::class, DbTrainingExercise::class, DbExerciseCategory::class],
    version = 6,
    exportSchema = false,
)
@ColumnTypeConverters(InstantConverters::class)
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
