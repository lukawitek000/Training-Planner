package com.lukasz.witkowski.shared.models.statistics

data class GeneralStatistics(
    val statisticsId: Long,
    val time: Long,
    val date: Long,
    val burnedCalories: Double,
    val maxHeartRate: Double,
    val heartRateDuringTraining: List<Double>
)