package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "Exercise")
internal class DbExercise(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val categoryId: Int,
    val imageId: String?
)
