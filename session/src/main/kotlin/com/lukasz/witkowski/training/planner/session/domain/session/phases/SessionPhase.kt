package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface SessionPhase : AutoCloseable {
    val state: Flow<PhaseState>
    fun complete()
    fun skip()
    fun handle(command: SessionCommand)
}