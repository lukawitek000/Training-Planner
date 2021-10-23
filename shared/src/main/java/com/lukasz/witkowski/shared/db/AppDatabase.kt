package com.lukasz.witkowski.shared.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lukasz.witkowski.shared.models.Exercise

@Database(entities = [Exercise::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}