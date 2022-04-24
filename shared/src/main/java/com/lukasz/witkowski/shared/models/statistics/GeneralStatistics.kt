package com.lukasz.witkowski.shared.models.statistics

import com.lukasz.witkowski.shared.time.Time

data class GeneralStatistics(
    val statisticsId: Long,
    val time: Time,
    val date: Time,
    val burnedCalories: Double,
    val maxHeartRate: Double,
    val heartRateDuringTraining: List<Double>
)