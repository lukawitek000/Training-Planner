package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DbExercise::class], version = 4, exportSchema = false)
internal abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}