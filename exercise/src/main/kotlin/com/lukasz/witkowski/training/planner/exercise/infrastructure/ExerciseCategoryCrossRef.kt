package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.Index

@Entity(
    primaryKeys = ["exerciseId", "categoryName"],
    indices = [Index("exerciseId", "categoryName")]
)
data class ExerciseCategoryCrossRef(
    val exerciseId: String,
    val categoryName: String
)