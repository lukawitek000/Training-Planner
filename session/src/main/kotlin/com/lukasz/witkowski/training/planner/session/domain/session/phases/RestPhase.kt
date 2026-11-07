package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class RestPhase(
    val initialRestTime: Duration,
    private val nextExercise: SessionExercise,
    timerFactory: TimerFactory = TimerFactory()
) : SessionPhase {
    val restTimeDuration: Duration
        get() = initialRestTime
    private val timer = timerFactory.create(TimerState.Ready(initialRestTime))
    private val selectedRpe = MutableStateFlow<Int?>(null)

    override val state: Flow<PhaseState> =
        combine(
            timer.state,
            selectedRpe
        ) { timerState, rpe ->
            PhaseState.RestTime(
                nextExercise = nextExercise,
                timerState = timerState,
                totalRestTime = initialRestTime,
                selectedFeedback = rpe
            )
        }

    override val hasAutoFinished: Flow<Boolean> =
        timer.state.map { it == TimerState.Finished }

    override fun start(): SessionEvent {
        timer.start()
        return SessionEvent.RestStarted(restTimeDuration)
    }

    override fun complete(): SessionEvent.RestCompleted {
        close()
        return SessionEvent.RestCompleted(selectedRpe.value)
    }

    override fun skip(): SessionEvent.RestSkipped {
        close()
        return SessionEvent.RestSkipped(selectedRpe.value)
    }

    override fun endEarly(): SessionEvent.RestCompleted = complete()

    override fun handle(command: SessionCommand) {
        if (command !is SessionCommand.RestTime) {
            Timber.w("Invalid command for rest phase: $command")
            return
        }
        when (command) {
            is SessionCommand.RestTime.UpdateRpeFeedback -> {
                selectedRpe.value = command.rpe
            }
        }
    }

    override fun close() {
        timer.close()
    }
}
