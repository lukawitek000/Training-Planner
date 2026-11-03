package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

/**
 * Composite pattern implementation of [SessionStatisticsRecorder].
 * Dispatches session events to all registered recorders (e.g. Basic recorder, HeartRate recorder, VBT recorder).
 */
class CompositeStatisticsRecorder(
    private val recorders: List<SessionStatisticsRecorder> = emptyList()
) : SessionStatisticsRecorder {

    constructor(vararg recorders: SessionStatisticsRecorder) : this(recorders.toList())

    override fun onEvent(event: SessionEvent) {
        recorders.forEach { it.onEvent(event) }
    }
}
