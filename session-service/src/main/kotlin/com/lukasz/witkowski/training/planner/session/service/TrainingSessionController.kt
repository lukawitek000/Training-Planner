package com.lukasz.witkowski.training.planner.session.service

import android.content.Context
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.presentation.toPresentationTrainingSessionState
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class TrainingSessionController(private val context: Context) {
    private val statisticsContainer: StatisticsContainer by lazy {
        StatisticsContainer.getInstance(
            context
        )
    }
    private val trainingSessionService: TrainingSessionService by lazy {
        statisticsContainer.trainingSessionService
    }
    private val trainingStatisticsService: TrainingStatisticsService by lazy {
        statisticsContainer.trainingStatisticsService
    }
    private val coroutineScope =
        CoroutineScope(Dispatchers.Default + CoroutineName("TrainingSessionController"))

    val trainingPlan: TrainingPlan
        get() = checkNotNull(trainingSessionService.trainingPlan) {
            "TrainingSession was not started, the training plan is null"
        }

    private val sessionFinishedListeners = mutableSetOf<SessionFinishedListener>()

    fun observeSessionState() = coroutineScope.launch {
        trainingSessionService.trainingSessionState.map {
            it.toPresentationTrainingSessionState()
        }.collectLatest {
            if (it is TrainingSessionState.SummaryState) {
                handleSummaryState(it)
            }
        }
    }

    fun addSessionFinishedListener(sessionFinishedListener: SessionFinishedListener) {
        sessionFinishedListeners.add(sessionFinishedListener)
    }

    fun removeSessionFinishedListener(sessionFinishedListener: SessionFinishedListener) {
        sessionFinishedListeners.remove(sessionFinishedListener)
    }

    private fun notifySessionFinished(trainingStatisticsId: TrainingStatisticsId) {
        sessionFinishedListeners.forEach {
            it.onSessionFinished(trainingStatisticsId)
        }
    }

    fun destroy() {
        coroutineScope.cancel()
    }

    private fun handleSummaryState(state: TrainingSessionState.SummaryState) {
        coroutineScope.launch {
            trainingStatisticsService.save(state.statistics)
            notifySessionFinished(state.statistics.id)
        }
    }
}
