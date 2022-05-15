package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Exercise")
internal data class DbExercise(
    @PrimaryKey
    val id: String,
    val trainingId: String,
    val name: String,
    val description: String,
    val categoryId: Int,
    val image: ByteArray?,
    val repetitions: Int,
    val sets: Int,
    val time: Long,
    val restTime: Long
)
