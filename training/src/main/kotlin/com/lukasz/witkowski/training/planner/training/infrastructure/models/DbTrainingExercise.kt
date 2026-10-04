package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "TrainingExercise")
data class DbTrainingExercise(
    @PrimaryKey
    val id: String,
    val trainingId: String,
    @Embedded
    val exercise: DbExerciseSnapshot,
    val repetitions: Int,
    val sets: Int,
    val restTime: Long,
)
