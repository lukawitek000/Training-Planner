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
    @Embedded
    val exercise: Exercise,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Long = 0L,
    val restTime: Long = 0L // Rest time after exercise
)
