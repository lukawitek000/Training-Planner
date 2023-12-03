package com.lukasz.witkowski.training.planner.session.service

import android.content.Context
import com.lukasz.witkowski.training.planner.statistics.application.SessionFinishedListener
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

internal class TrainingSessionController(private val context: Context, private val onSessionFinished: () -> Unit): SessionFinishedListener {

    private val statisticsContainer: StatisticsContainer by lazy {
        StatisticsContainer.getInstance(context)
    }

    private val trainingSessionService: TrainingSessionService by lazy {
        statisticsContainer.trainingSessionService
    }

    val trainingPlan: TrainingPlan
        get() = checkNotNull(trainingSessionService.trainingPlan) {
            "TrainingSession was not started, the training plan is null"
        }

    override fun onSessionFinished(trainingStatisticsId: TrainingStatisticsId) {
        onSessionFinished()
    }

    fun onServiceCreate() {
        trainingSessionService.removeSessionFinishedListener(this)
    }

    fun onServiceDestroy() {
        trainingSessionService.addSessionFinishedListener(this)
    }
}
