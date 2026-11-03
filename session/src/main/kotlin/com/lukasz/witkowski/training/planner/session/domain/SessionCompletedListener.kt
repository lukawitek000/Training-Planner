package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics

/**
 * Output port exposed by the session module for consumers interested in completed workouts.
 * Decouples live session execution from persistence (Room SQLite, Health Connect, Cloud sync).
 */
fun interface SessionCompletedListener {
    suspend fun onSessionCompleted(statistics: TrainingStatistics)
}
