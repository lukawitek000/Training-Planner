package com.lukasz.witkowski.training.planner.session.service

import android.content.Context
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionStateMapper
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TrainingSessionController(private val context: Context) {
    private val trainingSessionService: TrainingSessionService by lazy {
        StatisticsContainer.getInstance(context).trainingSessionService
    }
    private val coroutineScope =
        CoroutineScope(Dispatchers.Default + CoroutineName("TrainingSessionController"))

    var serviceTimerController: ServiceTimerController? = null
        private set

    val trainingPlan: TrainingPlan
        get() = trainingSessionService.trainingPlan
            ?: throw IllegalStateException("TrainingSession was not started, the training plan is null")

    fun observeSessionState() = coroutineScope.launch {
        trainingSessionService.trainingSessionState.map {
            TrainingSessionStateMapper.toPresentation(it)
        }.collectLatest {
            resetTimerHelper()
            when (it) {
                is TrainingSessionState.ExerciseState -> handleExerciseState(it)
                is TrainingSessionState.RestTimeState -> handleRestTimeState(it)
                is TrainingSessionState.SummaryState -> Unit
                is TrainingSessionState.IdleState -> Unit
            }
        }
    }

    fun destroy() {
        coroutineScope.cancel()
    }

    private fun resetTimerHelper() {
        serviceTimerController?.cancel()
        serviceTimerController = null
    }

    private fun handleExerciseState(exerciseState: TrainingSessionState.ExerciseState) {
        serviceTimerController = createServiceTimerController()
        serviceTimerController?.setTimer(exerciseState.currentExercise.time)
        serviceTimerController?.observeHasFinished {
            serviceTimerController?.resetTimer()
        }
    }

    private fun handleRestTimeState(state: TrainingSessionState.RestTimeState) {
        serviceTimerController = createServiceTimerController()
        serviceTimerController?.setTimer(state.time)
        serviceTimerController?.startTimer()
        serviceTimerController?.observeHasFinished {
            trainingSessionService.skip()
        }
    }

    // TODO how to inject it, some provider interface???
    private fun createServiceTimerController() =
        ServiceTimerController(StatisticsContainer.getInstance(context).timerController())
}