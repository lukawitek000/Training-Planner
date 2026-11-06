package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data object IdlePhase: SessionPhase {
    override val state: StateFlow<PhaseState> = MutableStateFlow(PhaseState.Idle)

    override fun complete() = Unit

    override fun skip() = Unit

    override fun handle(command: SessionCommand) = Unit

    override fun close() = Unit
}