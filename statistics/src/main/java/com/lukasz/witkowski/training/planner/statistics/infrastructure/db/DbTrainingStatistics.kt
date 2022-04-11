package com.lukasz.witkowski.training.planner.statistics.infrastructure.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.DbExerciseStatistics

@Entity(tableName = "TrainingStatistics")
data class DbTrainingStatistics(
    @PrimaryKey
    val id: String,
    val trainingPlanId: String,
    val totalTime: Long,
    val date: Long,
    @Ignore
    val exercisesStatistics: List<DbExerciseStatistics>
)
