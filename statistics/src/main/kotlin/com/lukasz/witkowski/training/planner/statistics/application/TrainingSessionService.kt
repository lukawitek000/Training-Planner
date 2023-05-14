package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.timer.Timer
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrainingSessionService(
    private val timeProvider: TimeProvider,
    private val timer: Timer,
    private val trainingSetsStrategy: TrainingSetsPolicy = CircuitSetsPolicy(),
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private lateinit var trainingSession: TrainingSession
    private val scope = CoroutineScope(backgroundDispatcher)

    val isTimerRunning: StateFlow<Boolean>
        get() = timer.isRunning

    private var state: TrainingSessionState = TrainingSessionState.IdleState
        private set(value) {
            configureTimer(value)
            _trainingSessionState.value = value
            field = value
        }

    private val _trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)
    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = _trainingSessionState

    var trainingPlan: TrainingPlan? = null
        private set

    val time: StateFlow<Time>
        get() = timer.time

    fun startTraining(trainingPlan: TrainingPlan) {
        this.trainingPlan = trainingPlan
        trainingSession = TrainingSession(trainingPlan, trainingSetsStrategy)
        state = trainingSession.start(timeProvider.currentTime())
    }

    fun skip() {
        stopTimer()
        state = trainingSession.skip(timeProvider.currentTime())
    }

    fun completed() {
        stopTimer()
        state = trainingSession.completed(timeProvider.currentTime())
    }

    fun stopTraining() {
        trainingSession.stop()
        state = TrainingSessionState.IdleState
    }

    fun isTrainingSessionStarted() = trainingSessionState.value !is TrainingSessionState.IdleState

    fun startTimer() {
        if(timer.isPaused) {
            timer.resume()
        } else {
            observeTimerFinished()
            timer.start()
        }
    }

    fun stopTimer() {
        timer.stop()
    }

    fun pauseTimer() {
        timer.pause()
    }

    private fun observeTimerFinished() {
        scope.launch {
            timer.hasFinished.collectLatest {
                if (it) {
                    navigateNextIfRestTimeElapsed()
                    resetTimerIfExerciseTimeElapsed()
                }
            }
        }
    }

    private fun resetTimerIfExerciseTimeElapsed() {
        if (state is TrainingSessionState.ExerciseState) {
            timer.stop()
        }
    }

    private fun navigateNextIfRestTimeElapsed() {
        if (state is TrainingSessionState.RestTimeState) {
            skip()
        }
    }

    private val TrainingSessionState.time: Time?
        get() = when(this) {
            is TrainingSessionState.RestTimeState -> restTime
            is TrainingSessionState.ExerciseState -> exercise?.time
            else -> null
        }

    private fun configureTimer(value: TrainingSessionState) {
        stopTimer()
        value.time?.let { timer.setTime(it) }
        if (value is TrainingSessionState.RestTimeState) {
            startTimer()
        }
    }
}
