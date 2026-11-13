package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId

/**
 * Output port exposed by the session module for consumers interested in completed workouts.
 * Decouples live session execution from persistence (Room SQLite, Health Connect, Cloud sync).
 */
interface SessionCompletedListener {
    suspend fun onSessionCompleted(statistics: TrainingStatistics)

    suspend fun onSessionMetadataUpdated(id: TrainingStatisticsId, rpe: SessionRpe?, note: String?)

    object NoOp : SessionCompletedListener {
        override suspend fun onSessionCompleted(statistics: TrainingStatistics) = Unit
        override suspend fun onSessionMetadataUpdated(
            id: TrainingStatisticsId,
            rpe: SessionRpe?,
            note: String?
        ) = Unit
    }
}
