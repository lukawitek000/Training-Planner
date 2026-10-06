package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "TrainingExercise",
    foreignKeys = [
        ForeignKey(
            entity = DbTrainingPlan::class,
            parentColumns = ["id"],
            childColumns = ["trainingId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("trainingId"),
    ],
)
data class DbTrainingExercise(
    @PrimaryKey
    val id: String,
    val trainingId: String,
    val exerciseId: String,
    val position: Int,
    val name: String,
    val description: String,
    val imagePath: String?,
    val repetitions: Int,
    val sets: Int,
    val restTime: Long,
    val weightInKg: Int?
)
