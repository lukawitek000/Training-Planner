package com.lukasz.witkowski.training.planner.session.application

import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionFactory
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class TrainingSessionService(
    private val timeProvider: TimeProvider = SystemTimeProvider(),
    private val timerFactory: TimerFactory = TimerFactory(timeProvider = timeProvider),
    private val trainingSetsStrategy: TrainingSetsPolicy = StraightSetsPolicy(),
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val sessionStatisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder()
) {
    private val activeSession = MutableStateFlow<TrainingSession?>(null)
    private lateinit var trainingSession: TrainingSession
    private val scope = CoroutineScope(backgroundDispatcher)

    val trainingSessionState: StateFlow<TrainingSessionState> =
        activeSession
            .flatMapLatest { session ->
                session?.state ?: flowOf(TrainingSessionState.IdleState)
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = TrainingSessionState.IdleState
            )

    var trainingPlan: TrainingPlan? = null
        private set


    fun startTraining(
        trainingPlan: TrainingPlan,
        policy: TrainingSetsPolicy = trainingSetsStrategy
    ) {
        this.trainingPlan = trainingPlan
        trainingSession =
            TrainingSessionFactory.create(
                trainingPlan = trainingPlan,
                policy = policy,
                statisticsRecorder = sessionStatisticsRecorder,
                timeProvider = timeProvider,
                timerFactory = timerFactory,
                scope = scope
            )
        activeSession.value = trainingSession
        trainingSession.start()
    }

    fun skip() {
        requireActiveSession().skip()
    }

    fun completed() {
        requireActiveSession().complete()
    }

    fun changeReps(newReps: Int) {
        requireActiveSession().updateReps(newReps)
    }

    fun changeWeight(weight: Double) {
        requireActiveSession().updateWeight(weight)
    }

    fun addExtraSet() {
        requireActiveSession().addExtraSet()
    }

    fun removeExtraSet() {
        requireActiveSession().removeExtraSet()
    }

    fun setExerciseSetFeedback(effort: SetEffort) {
        requireActiveSession().handle(SessionCommand.RestTime.UpdateFeedback(effort))
    }

    fun updateHoldTime(time: Duration) {
        requireActiveSession().handle(SessionCommand.Exercise.UpdateHoldDuration(time))
    }

    fun startTimer() {
        requireActiveSession().handle(SessionCommand.Exercise.StartTimer)
    }

    fun resetTimer() {
        requireActiveSession().handle(SessionCommand.Exercise.ResetTimer)
    }

    fun endEarly() {
        requireActiveSession().endEarly()
    }

    fun handle(command: SessionCommand) {
        requireActiveSession().handle(command)
    }

    private fun requireActiveSession(): TrainingSession {
        check(::trainingSession.isInitialized && activeSession.value != null) {
            "Training session has not been started or was stopped"
        }
        return trainingSession
    }
}
