package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "Exercise")
class DbExercise(
    @PrimaryKey
    val exerciseId: String,
    val name: String,
    val description: String,
    val imageId: String?,
)
