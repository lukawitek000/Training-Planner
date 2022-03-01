package com.lukasz.witkowski.shared.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrainingExercise(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TrainingExerciseId")
    val id: Long = 0L,
    var trainingId: Long = 0L,
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Long = 0L,
    var restTime: Long = 0L // Rest time after exercise
)
