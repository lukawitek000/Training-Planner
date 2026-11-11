package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.timer.Timer
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class ExercisePhase(
    loadedExercise: SessionExercise,
    private val timerFactory: TimerFactory = TimerFactory()
) : SessionPhase {
    private val timer: Timer = loadedExercise.createTimer()
    private val exercise = MutableStateFlow(loadedExercise)
    val currentExercise: SessionExercise
        get() = exercise.value

    val restTime: Duration
        get() = exercise.value.restTime

    override val state: Flow<PhaseState> =
        combine(
            timer.state,
            exercise
        ) { timerState, exercise ->
            PhaseState.Exercise(
                exercise = exercise,
                timerState = timerState
            )
        }

    override fun start(): SessionEvent = SessionEvent.ExerciseStarted(currentExercise)

    override fun complete(): SessionEvent.ExerciseCompleted {
        close()
        return SessionEvent.ExerciseCompleted(currentExercise)
    }

    override fun skip(): SessionEvent.ExerciseSkipped {
        close()
        return SessionEvent.ExerciseSkipped(currentExercise)
    }

    override fun endEarly(): SessionEvent.ExerciseSkipped = skip()

    override fun handle(command: SessionCommand) {
        if (command !is SessionCommand.Exercise) {
            Timber.w("Invalid command for exercise phase: $command")
            return
        }
        when (command) {
            is SessionCommand.Exercise.UpdateReps -> {
                exercise.updateConfig { it.copy(reps = command.reps) }
            }

            is SessionCommand.Exercise.UpdateWeight -> {
                exercise.updateConfig { it.copy(weightInKg = command.weightInKg) }
            }

            is SessionCommand.Exercise.UpdateHoldDuration -> {
                exercise.updateConfig { it.copy(holdTime = command.duration) }
                timer.changeTime(duration = command.duration)
            }

            is SessionCommand.Exercise.StartTimer -> {
                timer.start()
            }

            is SessionCommand.Exercise.ResetTimer -> {
                timer.reset()
            }
        }
    }

    override fun close() {
        timer.close()
    }

    private fun MutableStateFlow<SessionExercise>.updateConfig(
        block: (SessionExerciseConfiguration) -> SessionExerciseConfiguration
    ) {
        this.update { it.copy(currentConfig = block(it.currentConfig)) }
    }

    private fun SessionExercise.createTimer(): Timer {
        val initialState =
            currentConfig.holdTime
                ?.takeIf { it > Duration.ZERO }
                ?.let { TimerState.Ready(it) }
                ?: TimerState.NotSet
        return timerFactory.create(initialState, allowOverflow = true)
    }
}
