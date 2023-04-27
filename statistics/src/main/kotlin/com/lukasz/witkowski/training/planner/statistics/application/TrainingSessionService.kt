package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.timer.Timer
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
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
) {

    private lateinit var trainingSession: TrainingSession
    private val scope = CoroutineScope(Dispatchers.Default)

    private var _isTimerRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean>
        get() = _isTimerRunning

    var state: TrainingSessionState = TrainingSessionState.IdleState
        private set(value) {
            if (value is TrainingSessionState.RestTimeState) {
                timer.setTime(value.restTime)
            } else if (value is TrainingSessionState.ExerciseState) {
                value.exercise?.let { timer.setTime(it.time) }
            }
            if (value is TrainingSessionState.RestTimeState) {
                timer.start()
            }
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
        _trainingSessionState.value = trainingSession.start(timeProvider.currentTime())
    }

    fun skip() {
        stopTimer()
        _trainingSessionState.value = trainingSession.skip(timeProvider.currentTime())
    }

    fun completed() {
        stopTimer()
        _trainingSessionState.value = trainingSession.completed(timeProvider.currentTime())
    }

    fun stopTraining() {
        trainingSession.stop()
        _trainingSessionState.value = TrainingSessionState.IdleState
    }

    fun isTrainingSessionStarted() = trainingSessionState.value !is TrainingSessionState.IdleState

    fun startTimer() {
        _isTimerRunning.value = true
        observeTimer()
        timer.start()
    }
    fun pauseTimer() {
        _isTimerRunning.value = false
        timer.pause()
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        timer.stop()
    }


    private fun observeTimer() {
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
}
