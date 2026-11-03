package com.lukasz.witkowski.training.planner.session.application

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId

fun interface SessionFinishedListener {
    fun onSessionFinished(trainingStatisticsId: TrainingStatisticsId)
}
