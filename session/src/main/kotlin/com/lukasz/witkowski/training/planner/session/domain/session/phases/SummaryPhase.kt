package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class SummaryPhase(private val statisticsProvider: SessionStatisticsProvider) :
    SessionPhase {
    private val _state by lazy {
        MutableStateFlow(
            PhaseState.Summary(
                statistics = statisticsProvider.getStatistics()
            )
        )
    }
    override val state: StateFlow<PhaseState>
        get() = _state

    override fun start(): SessionEvent = SessionEvent.SessionFinished

    override fun complete(): SessionEvent.SummaryCompleted {
        val current = _state.value
        return SessionEvent.SummaryCompleted(
            overallRpe = current.selectedOverallRpe,
            notes = current.notes
        )
    }

    override fun skip(): SessionEvent.SummaryCompleted = complete()

    override fun handle(command: SessionCommand) {
        if (command !is SessionCommand.Summary) {
            Timber.w("Invalid command for summary phase: $command")
            return
        }
        when (command) {
            is SessionCommand.Summary.UpdateOverallRpe -> {
                _state.update { it.copy(selectedOverallRpe = command.rpe) }
            }

            is SessionCommand.Summary.UpdateNotes -> {
                _state.update { it.copy(notes = command.notes) }
            }
        }
    }

    override fun close() = Unit
}
