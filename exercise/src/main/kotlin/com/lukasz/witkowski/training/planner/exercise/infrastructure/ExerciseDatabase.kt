package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [DbExercise::class, DbExerciseCategory::class, ExerciseCategoryCrossRef::class,
        DbExerciseRecommendation::class],
    version = 8,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
internal abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun categoryDao(): ExerciseCategoryDao

    companion object {
        @Volatile
        private var instance: ExerciseDatabase? = null

        fun getInstance(context: Context): ExerciseDatabase =
            synchronized(this) {
                if (instance == null) {
                    instance =
                        Room
                            .databaseBuilder(
                                context,
                                ExerciseDatabase::class.java,
                                "ExerciseDb",
                            )
                            .createFromAsset("database/exercises.db") // TODO regenerate default exercises
//                            .fallbackToDestructiveMigration(true)
                            .build()
                }
                instance!!
            }
    }
}
