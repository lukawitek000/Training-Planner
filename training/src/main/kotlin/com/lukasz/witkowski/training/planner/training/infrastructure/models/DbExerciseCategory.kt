package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Entity

@Entity("DbExerciseCategory")
data class DbExerciseCategory(
    val name: String
)