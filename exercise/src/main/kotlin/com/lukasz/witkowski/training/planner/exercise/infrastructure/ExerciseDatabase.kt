package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [DbExercise::class, DbExerciseCategory::class, ExerciseCategoryCrossRef::class],
    version = 6,
    exportSchema = false
)
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
                            .createFromAsset("database/exercises.db")
//                            .fallbackToDestructiveMigration(true)
                            .build()
                }
                instance!!
            }
    }
}
