package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.timer.Timer
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.time.Duration

class RestPhase(
    private val initialRestTime: Duration,
    private val nextExercise: SessionExercise,
    private val statisticsRecorder: SessionStatisticsRecorder,
): SessionPhase {
    private val timer = Timer(TimerState.Ready(initialRestTime))
    override val state: Flow<PhaseState> = timer.state.map<TimerState, PhaseState> {
        PhaseState.RestTime(
            nextExercise = nextExercise,
            timerState = it,
            totalRestTime = initialRestTime,
        )
    }

    init {
        timer.start()
    }

    override fun complete() {
        TODO("Not yet implemented")
    }

    override fun skip() {
        TODO("Not yet implemented")
    }

    override fun handle(command: SessionCommand) {
        if (command !is SessionCommand.RestTime) {
            Timber.w("Invalid command for rest phase: $command")
            return
        }
        when (command) {
            is SessionCommand.RestTime.UpdateRpeFeedback -> {
//                statisticsRecorder.onEvent(SessionEvent.)
            }
        }
    }

    override fun close() {
        timer.close()
    }
}