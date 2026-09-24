package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class DbExerciseCategory(
    @PrimaryKey val categoryName: String,
)
