package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import java.util.concurrent.atomic.AtomicReference

/**
 * [BasicStatisticsRecorder] records standard session metrics:
 *  * Total training time
 *  * Exercise attempt durations
 *  * Attempt sets, reps, load, hold time, rest time, RPE, and completion status
 *  * Date of the workout session
 *
 * Implemented using a lock-free atomic progressive state reducer ([SessionMetricsAccumulator]).
 */
class BasicStatisticsRecorder(private val timeProvider: TimeProvider = SystemTimeProvider()) :
    SessionStatisticsRecorder,
    SessionStatisticsProvider {

    private val state = AtomicReference<SessionMetricsAccumulator?>(null)

    override fun onEvent(event: SessionEvent) {
        val currentElapsed = timeProvider.elapsedTime()
        val currentDate = timeProvider.currentDate()
        state.updateAndGet { current ->
            if (event is SessionEvent.SessionStarted) {
                SessionMetricsAccumulator.start(event, currentElapsed, currentDate)
            } else {
                current?.reduce(event, currentElapsed, currentDate)
            }
        }
    }

    override fun getStatistics(): TrainingStatistics {
        val accumulator =
            checkNotNull(state.get()) {
                "Cannot retrieve statistics before SessionStarted event has been recorded."
            }
        return accumulator.toTrainingStatistics(timeProvider.elapsedTime())
    }
}
