package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Exercise")
internal class DbExercise(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val categoryId: Int,
    val imagePath: String?
)