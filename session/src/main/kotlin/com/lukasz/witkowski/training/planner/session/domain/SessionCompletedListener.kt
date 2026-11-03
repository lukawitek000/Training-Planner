package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics

/**
 * Output port exposed by the session module for consumers interested in completed workouts.
 * Allows :session to remain decoupled from persistent storage (local Room, Health Connect, Cloud Sync).
 */
interface SessionCompletedListener {
    suspend fun onSessionCompleted(statistics: TrainingStatistics)
}
