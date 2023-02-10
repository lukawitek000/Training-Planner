package com.lukasz.witkowski.training.planner.session.service

import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId

fun interface SessionFinishedListener {
    fun onSessionFinished(trainingStatisticsId: TrainingStatisticsId)
}
