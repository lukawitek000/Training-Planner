package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [
        DbExercise::class, DbExerciseCategory::class, ExerciseCategoryCrossRef::class,
        DbExerciseRecommendation::class, DbExerciseImage::class, DbSyncMetadata::class, DbExerciseMutation::class
    ],
    version = 14,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
internal abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    abstract fun categoryDao(): ExerciseCategoryDao

    abstract fun mutationDao(): ExerciseMutationDao

    companion object {
        @Volatile
        private var instance: ExerciseDatabase? = null

        fun getInstance(context: Context): ExerciseDatabase = synchronized(this) {
            if (instance == null) {
                cleanOrphanedOrCorruptDatabaseFiles(context, "ExerciseDb")
                instance =
                    Room
                        .databaseBuilder(
                            context,
                            ExerciseDatabase::class.java,
                            "ExerciseDb"
                        )
                        .createFromAsset("database/exercises.db")
                        .fallbackToDestructiveMigration(true)
                        .build()
            }
            instance!!
        }

        private fun cleanOrphanedOrCorruptDatabaseFiles(context: Context, databaseName: String) {
            val dbFile = context.getDatabasePath(databaseName)
            if (!dbFile.exists()) {
                context.deleteDatabase(databaseName)
                return
            }
            if (dbFile.length() < 512L) {
                context.deleteDatabase(databaseName)
                return
            }
            try {
                java.io.FileInputStream(dbFile).channel.use { channel ->
                    val buffer = java.nio.ByteBuffer.allocate(4)
                    channel.position(60)
                    val bytesRead = channel.read(buffer)
                    if (bytesRead != 4) {
                        context.deleteDatabase(databaseName)
                    }
                }
            } catch (_: Exception) {
                context.deleteDatabase(databaseName)
            }
        }
    }
}
