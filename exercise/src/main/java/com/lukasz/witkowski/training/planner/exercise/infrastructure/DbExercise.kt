package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Exercise")
class DbExercise(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val image: ByteArray?
)