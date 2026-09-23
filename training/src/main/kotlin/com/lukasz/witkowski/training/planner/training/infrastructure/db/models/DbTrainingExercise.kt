package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "Exercise")
data class DbTrainingExercise(
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
