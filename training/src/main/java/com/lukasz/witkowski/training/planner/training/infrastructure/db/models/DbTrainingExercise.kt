package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Exercise")
internal data class DbTrainingExercise(
    @PrimaryKey
    val id: String,
    val trainingId: String,
    @Embedded
    val exercise: DbExercise,
    val repetitions: Int,
    val sets: Int,
    val time: Long,
    val restTime: Long
)
