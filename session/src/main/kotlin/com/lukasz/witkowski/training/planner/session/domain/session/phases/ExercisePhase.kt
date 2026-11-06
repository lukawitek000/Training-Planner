package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.timer.Timer
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import timber.log.Timber
import kotlin.time.Duration

internal class ExercisePhase(
    loadedExercise: SessionExercise,
    private val statisticsRecorder: SessionStatisticsRecorder,
) : SessionPhase {
    private val timer: Timer = loadedExercise.createTimer()
    private val exercise = MutableStateFlow(loadedExercise)
    val currentExercise: SessionExercise
        get() = exercise.value

    val restTime: Duration
        get() = exercise.value.restTime

    override val state: Flow<PhaseState> = combine(
        timer.state,
        exercise
    ) { timerState, exercise ->
        PhaseState.Exercise(
            exercise = exercise,
            timerState = timerState
        )
    }

    override fun complete() {
        TODO("Not yet implemented")
    }

    override fun skip() {
        TODO("Not yet implemented")
    }

    override fun handle(command: SessionCommand) {
        if (command !is SessionCommand.Exercise) {
            Timber.w("Invalid command for exercise phase: $command")
            return
        }
        when (command) {
            is SessionCommand.Exercise.UpdateReps -> {
                exercise.updateConfig { it.copy(reps = command.reps) }
            }

            is SessionCommand.Exercise.UpdateHoldDuration -> {
                exercise.updateConfig { it.copy(holdTime = command.duration) }
                timer.changeTime(duration = command.duration)
            }

            is SessionCommand.Exercise.StartTimer -> {
                timer.start()
            }
        }
    }

    override fun close() {
        timer.close()
    }

    private fun MutableStateFlow<SessionExercise>.updateConfig(block: (SessionExerciseConfiguration) -> SessionExerciseConfiguration) {
        this.update { it.copy(currentConfig = block(it.currentConfig)) }
    }

    private fun SessionExercise.createTimer() : Timer {
        val initialState = currentConfig.holdTime?.let {
            TimerState.Ready(it)
        } ?: TimerState.NotSet
        return Timer(initialState, allowOverflow = true)
    }
}