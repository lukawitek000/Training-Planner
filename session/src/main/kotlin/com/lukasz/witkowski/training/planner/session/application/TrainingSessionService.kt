package com.lukasz.witkowski.training.planner.session.application

import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionFactory
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class TrainingSessionService(
    private val sessionCompletedListener: SessionCompletedListener = SessionCompletedListener.NoOp,
    private val timeProvider: TimeProvider = SystemTimeProvider(),
    private val timerFactory: TimerFactory = TimerFactory(timeProvider = timeProvider),
    private val trainingSetsStrategy: TrainingSetsPolicy = StraightSetsPolicy(),
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val sessionStatisticsRecorder: SessionStatisticsRecorder? = null
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + backgroundDispatcher)
    private var currentSessionScope: CoroutineScope? = null
    private var eagerSaveJob: Job? = null
    private var savedStatisticsId: TrainingStatisticsId? = null

    private val activeSession = MutableStateFlow<TrainingSession?>(null)
    private lateinit var trainingSession: TrainingSession

    val trainingSessionState: StateFlow<TrainingSessionState> =
        activeSession
            .flatMapLatest { session ->
                session?.state ?: flowOf(TrainingSessionState.IdleState)
            }.stateIn(
                scope = serviceScope,
                started = SharingStarted.Eagerly,
                initialValue = TrainingSessionState.IdleState
            )

    var trainingPlan: TrainingPlan? = null
        private set

    fun startTraining(
        trainingPlan: TrainingPlan,
        policy: TrainingSetsPolicy = trainingSetsStrategy
    ) {
        currentSessionScope?.cancel()
        val newSessionScope = CoroutineScope(
            SupervisorJob(serviceScope.coroutineContext.job) + backgroundDispatcher
        )
        currentSessionScope = newSessionScope

        savedStatisticsId = null
        eagerSaveJob = null
        this.trainingPlan = trainingPlan

        trainingSession =
            TrainingSessionFactory.create(
                trainingPlan = trainingPlan,
                policy = policy,
                statisticsRecorder = sessionStatisticsRecorder,
                timeProvider = timeProvider,
                timerFactory = timerFactory,
                scope = newSessionScope
            )
        activeSession.value = trainingSession

        trainingSession.state
            .filterIsInstance<TrainingSessionState.SummaryState>()
            .take(1)
            .onEach { summaryState ->
                savedStatisticsId = summaryState.statistics.id
                eagerSaveJob = serviceScope.launch(ioDispatcher) {
                    sessionCompletedListener.onSessionCompleted(summaryState.statistics)
                }
            }
            .launchIn(newSessionScope)

        trainingSession.start()
    }

    fun finishWorkout() {
        val session = activeSession.value ?: return
        val summaryState = session.state.value as? TrainingSessionState.SummaryState
        val statistics = summaryState?.statistics ?: return
        val rpe = summaryState.selectedOverallRpe
        val note = summaryState.notes.ifBlank { null }

        session.complete()

        val saveJob = eagerSaveJob ?: serviceScope.launch(ioDispatcher) {
            sessionCompletedListener.onSessionCompleted(statistics)
        }

        serviceScope.launch(ioDispatcher) {
            saveJob.join()
            sessionCompletedListener.onSessionMetadataUpdated(
                id = statistics.id,
                rpe = rpe,
                note = note
            )
        }

        currentSessionScope?.cancel()
        currentSessionScope = null
        activeSession.value = null
        this.trainingPlan = null
    }

    fun skip() {
        requireActiveSession().skip()
    }

    fun completed() {
        requireActiveSession().complete()
    }

    fun changeReps(newReps: Int) {
        requireActiveSession().handle(SessionCommand.Exercise.UpdateReps(newReps))
    }

    fun changeWeight(weight: Double) {
        requireActiveSession().handle(SessionCommand.Exercise.UpdateWeight(weight))
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

    fun updateSessionNotes(notes: String) {
        requireActiveSession().handle(SessionCommand.Summary.UpdateNotes(notes))
    }

    fun updateSessionFeedback(rpe: SessionRpe) {
        requireActiveSession().handle(SessionCommand.Summary.UpdateOverallRpe(rpe))
    }

    fun endEarly() {
        requireActiveSession().endEarly()
        currentSessionScope?.cancel()
        currentSessionScope = null
        activeSession.value = null
        this.trainingPlan = null
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
