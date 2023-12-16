package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId

fun interface SessionFinishedListener {
    fun onSessionFinished(trainingStatisticsId: TrainingStatisticsId)
}
