package com.lukasz.witkowski.training.planner.image.infrastructure.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DbImageReference::class, DbImageOwner::class],
    version = 2,
    exportSchema = false
)
internal abstract class ImageReferenceDatabase : RoomDatabase() {
    abstract fun imageReferenceDao(): ImageReferenceDao

    companion object {
        @Volatile
        private var instance: ImageReferenceDatabase? = null

        fun getInstance(context: Context): ImageReferenceDatabase {
            return synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        ImageReferenceDatabase::class.java,
                        "image-references-database"
                    ).fallbackToDestructiveMigration().build()
                }
                instance!!
            }
        }
    }
}
