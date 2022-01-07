package com.lukasz.witkowski.shared.models.statistics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lukasz.witkowski.shared.models.statistics.ExerciseStatistics

@Entity
data class TrainingStatistics(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TrainingIdStatistics")
    val id: Long = 0L,
    var trainingId: Long,
    var totalTime: Long = 0L,
    val date: Long
)