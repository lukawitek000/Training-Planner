package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.session.phases.IdlePhase
import com.lukasz.witkowski.training.planner.session.domain.session.phases.NextPhaseProvider
import com.lukasz.witkowski.training.planner.session.domain.session.phases.SessionPhase
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueue
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * Training session aggregate - controls state and progression of the training session.
 * Requires [TrainingPlan] with at least one [TrainingExercise].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
internal class TrainingSession(
    private val trainingPlanId: TrainingPlanId,
    private val exerciseQueue: ExerciseQueue,
    private val nextPhaseProvider: NextPhaseProvider,
    private val statisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder(),
    scope: CoroutineScope
) {
    private val currentPhase = MutableStateFlow<SessionPhase>(IdlePhase)

    val state: StateFlow<TrainingSessionState> =
        currentPhase
            .flatMapLatest { phase ->
                combine(phase.state, exerciseQueue.progress) { phaseState, queueProgress ->
                    phaseState.toTrainingSessionState(queueProgress)
                }
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = TrainingSessionState.IdleState
            )

    init {
        currentPhase
            .flatMapLatest { it.hasAutoFinished }
            .filter { it }
            .onEach { complete() }
            .launchIn(scope)
    }

    fun start() {
        if (currentPhase.value !is IdlePhase) return
        statisticsRecorder.onEvent(SessionEvent.SessionStarted(trainingPlanId))
        transitionToNextPhase()
    }

    fun complete() {
        onPhaseFinished(currentPhase.value.complete())
    }

    fun skip() {
        onPhaseFinished(currentPhase.value.skip())
    }

    fun endEarly() {
        val event = currentPhase.value.endEarly() ?: return
        recordPhaseEvent(event)
        transitionToPhase(nextPhaseProvider.summaryPhase())
        currentPhase.value = IdlePhase
    }

    fun addExtraSet() {
        val (exercise, newTotal) = exerciseQueue.addExtraSet()
        statisticsRecorder.onEvent(SessionEvent.ExtraSetAdded(exercise, newTotal))
    }

    fun removeExtraSet() {
        exerciseQueue.removeExtraSet()
    }

    fun handle(command: SessionCommand) {
        currentPhase.value.handle(command)
    }

    fun updateReps(newReps: Int) {
        currentPhase.value.handle(SessionCommand.Exercise.UpdateReps(newReps))
    }

    fun updateWeight(weight: Double) {
        currentPhase.value.handle(SessionCommand.Exercise.UpdateWeight(weight))
    }

    private fun onPhaseFinished(event: SessionEvent?) {
        if (event == null) return
        recordPhaseEvent(event)
        if (event !is SessionEvent.SummaryCompleted) {
            transitionToNextPhase()
        }
    }

    private fun recordPhaseEvent(event: SessionEvent) {
        if (event is SessionEvent.ExerciseCompleted) {
            exerciseQueue.updateConfigForRemaining(
                event.exercise.exerciseId,
                event.exercise.currentConfig
            )
        }
        statisticsRecorder.onEvent(event)
    }

    private fun transitionToNextPhase() {
        val nextPhase = nextPhaseProvider.nextPhase(currentPhase.value)
        transitionToPhase(nextPhase)
    }

    private fun transitionToPhase(nextPhase: SessionPhase) {
        currentPhase.value.close()
        nextPhase.start()?.let(statisticsRecorder::onEvent)
        currentPhase.value = nextPhase
    }
}
