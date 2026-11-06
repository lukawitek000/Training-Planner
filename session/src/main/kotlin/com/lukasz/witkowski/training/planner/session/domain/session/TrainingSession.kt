package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.session.phases.IdlePhase
import com.lukasz.witkowski.training.planner.session.domain.session.phases.NextPhaseProvider
import com.lukasz.witkowski.training.planner.session.domain.session.phases.PhaseState
import com.lukasz.witkowski.training.planner.session.domain.session.phases.SessionPhase
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueue
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

/**
 * Training session aggregate - controls state and progression of the training session.
 * Requires [TrainingPlan] with at least one [TrainingExercise].
 */
@Suppress("TooManyFunctions")
internal class TrainingSession(
    private val exerciseQueue: ExerciseQueue,
    private val nextPhaseProvider: NextPhaseProvider,
    private val scope: CoroutineScope,
) {
    private val currentPhase = MutableStateFlow<SessionPhase>(IdlePhase)

    val state: StateFlow<TrainingSessionState> = combine(
        currentPhase.flatMapLatest { it.state },
        exerciseQueue.progress
    ) { state, queueProgress ->
        when (state) {
            is PhaseState.Idle -> TrainingSessionState.IdleState
            is PhaseState.Exercise -> TrainingSessionState.ExerciseState(
                sessionExercise = state.exercise,
                timerState = state.timerState,
                progress = queueProgress,
            )
            is PhaseState.RestTime -> {
                if (state.timerState == TimerState.Finished) {
                    transitionToNextPhase()
                }
                TrainingSessionState.RestTimeState(
                    nextExercise = state.nextExercise,
                    timerState = state.timerState,
                    totalRestTime = Duration.ZERO,
                    progress = queueProgress
                )
            }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = TrainingSessionState.IdleState
    )

    fun start() {
        transitionToNextPhase()
    }

    fun complete() {
        currentPhase.value.complete()
    }

    fun skip() {
        currentPhase.value.skip()
    }

    private fun transitionToNextPhase() {
        currentPhase.value.close()
        currentPhase.value = nextPhaseProvider.nextPhase(currentPhase.value)
    }
}
