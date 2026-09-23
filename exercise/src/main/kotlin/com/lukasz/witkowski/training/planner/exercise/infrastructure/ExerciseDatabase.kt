package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(entities = [DbExercise::class], version = 6, exportSchema = false)
internal abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var instance: ExerciseDatabase? = null

        fun getInstance(context: Context): ExerciseDatabase {
            return synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        ExerciseDatabase::class.java,
                        "Exercise Database"
                    )
                        .fallbackToDestructiveMigration(true)
                        .build()
                }
                instance!!
            }
        }
    }
}
