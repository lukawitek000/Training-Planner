package com.lukasz.witkowski.shared.models.statistics

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExerciseStatistics(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ExerciseStatisticsId")
    val id: Long = 0L,
    val trainingStatisticsId: Long = 0L,
    val trainingExerciseId: Long,
    @Embedded
    val heartRateStatistics: HeartRateStatistics,
    @Embedded
    val burntCaloriesStatistics: CaloriesStatistics,
    val averageTime: Long
)
