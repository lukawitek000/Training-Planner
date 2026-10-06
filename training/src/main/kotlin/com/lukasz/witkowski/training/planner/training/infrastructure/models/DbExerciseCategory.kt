package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "exercise_categories",
    primaryKeys = ["trainingExerciseId", "name"],
    foreignKeys = [
        ForeignKey(
            entity = DbTrainingPlan::class,
            parentColumns = ["id"],
            childColumns = ["trainingPlanId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbTrainingExercise::class,
            parentColumns = ["id"],
            childColumns = ["trainingExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("trainingPlanId"),
        Index("trainingExerciseId"),
    ],
)
data class DbExerciseCategory(
    val trainingExerciseId: String,
    val trainingPlanId: String,
    val name: String
)
