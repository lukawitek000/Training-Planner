package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import kotlinx.coroutines.flow.StateFlow

class SummaryPhase: SessionPhase {
    override val state: StateFlow<PhaseState>
        get() = TODO("Not yet implemented")

    override fun complete() {
        TODO("Not yet implemented")
    }

    override fun skip() {
        TODO("Not yet implemented")
    }

    override fun handle(command: SessionCommand) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}