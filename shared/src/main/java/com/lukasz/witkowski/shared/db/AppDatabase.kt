package com.lukasz.witkowski.shared.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lukasz.witkowski.shared.models.Exercise

@Database(entities = [Exercise::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}