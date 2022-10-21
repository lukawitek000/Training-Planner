package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DbImageReference::class, DbImageOwner::class], version = 1)
internal abstract class ImageReferenceDatabase : RoomDatabase() {
    abstract fun imageReferenceDao(): ImageReferenceDao
}
