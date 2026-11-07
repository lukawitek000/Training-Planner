package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal interface SessionPhase : AutoCloseable {
    val state: Flow<PhaseState>
    val hasAutoFinished: Flow<Boolean>
        get() = emptyFlow()

    fun start(): SessionEvent? = null

    fun complete(): SessionEvent? = null

    fun skip(): SessionEvent? = null

    fun endEarly(): SessionEvent? = null

    fun handle(command: SessionCommand)
}
