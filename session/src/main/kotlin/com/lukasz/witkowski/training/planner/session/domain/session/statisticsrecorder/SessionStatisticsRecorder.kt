package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

/**
 * Interface for recording session statistics and sensor readings.
 * Implementations can listen to [SessionEvent] to record metrics such as heart rate,
 * velocity based training (VBT), wearable sensor data, or basic completion data.
 */
fun interface SessionStatisticsRecorder {
    fun onEvent(event: SessionEvent)
}
