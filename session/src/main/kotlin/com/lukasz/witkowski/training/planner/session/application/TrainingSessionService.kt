package com.lukasz.witkowski.training.planner.session.application

import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.Timer
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration
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
    private val sessionCompletedListener: SessionCompletedListener? = null,
    private val trainingSetsStrategy: TrainingSetsPolicy = StraightSetsPolicy(),
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val sessionStatisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder()
) {
    private lateinit var trainingSession: TrainingSession
    private val scope = CoroutineScope(backgroundDispatcher)

    val isTimerRunning: StateFlow<Boolean>
        get() = timer.isRunning

    private var state: TrainingSessionState
        private set(value) {
            configureTimer(value)
            _trainingSessionState.value = value
            saveTrainingSessionStatistics(value)
        }
        get() = _trainingSessionState.value

    private val _trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)
    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = _trainingSessionState

    var trainingPlan: TrainingPlan? = null
        private set

    val time: StateFlow<Time>
        get() = timer.time

    private val trainingSessionFinishedListeners = mutableSetOf<SessionFinishedListener>()

    fun startTraining(
        trainingPlan: TrainingPlan,
        policy: TrainingSetsPolicy = trainingSetsStrategy
    ) {
        this.trainingPlan = trainingPlan
        trainingSession = TrainingSession(
            trainingPlan = trainingPlan,
            trainingSetsPolicy = policy,
            statisticsRecorder = sessionStatisticsRecorder
        )
        state = trainingSession.start(timeProvider.currentTime())
    }

    fun skip() {
        stopTimer()
        state = trainingSession.skip(timeProvider.currentTime())
    }

    fun skipExercise() {
        stopTimer()
        state = trainingSession.skipExercise(timeProvider.currentTime())
    }

    fun addExtraSet() {
        state = trainingSession.addExtraSet()
    }

    fun completed(
        actualReps: Int? = null,
        actualWeightInKg: Double? = null,
        rpe: Float? = null,
        actualTime: Duration? = null
    ) {
        stopTimer()
        state = trainingSession.completed(
            time = timeProvider.currentTime(),
            actualReps = actualReps,
            actualWeightInKg = actualWeightInKg,
            rpe = rpe,
            actualTime = actualTime
        )
    }

    fun stopTraining() {
        if (::trainingSession.isInitialized) {
            trainingSession.stop()
        }
        state = TrainingSessionState.IdleState
    }

    fun isTrainingSessionStarted() = trainingSessionState.value !is TrainingSessionState.IdleState

    fun startTimer() {
        if (timer.isPaused) {
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

    fun addSessionFinishedListener(sessionFinishedListener: SessionFinishedListener) {
        require(trainingSessionFinishedListeners.add(sessionFinishedListener)) {
            LISTENER_ALREADY_ADDED_MESSAGE
        }
    }

    fun removeSessionFinishedListener(sessionFinishedListener: SessionFinishedListener) {
        trainingSessionFinishedListeners.remove(sessionFinishedListener)
    }

    private fun saveTrainingSessionStatistics(state: TrainingSessionState) {
        if (state is TrainingSessionState.SummaryState) {
            scope.launch {
                sessionCompletedListener?.onSessionCompleted(state.statistics)
                notifySessionFinished(state.statistics.id)
            }
        }
    }

    private fun notifySessionFinished(trainingStatisticsId: TrainingStatisticsId) {
        trainingSessionFinishedListeners.forEach {
            it.onSessionFinished(trainingStatisticsId)
        }
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
        get() =
            when (this) {
                is TrainingSessionState.RestTimeState -> restTime
                else -> null
            }

    private fun configureTimer(value: TrainingSessionState) {
        stopTimer()
        value.time?.let { timer.setTime(it) }
        if (value is TrainingSessionState.RestTimeState) {
            startTimer()
        }
    }

    companion object {
        private const val LISTENER_ALREADY_ADDED_MESSAGE =
            "The SessionFinishedListener was already added"
    }
}
