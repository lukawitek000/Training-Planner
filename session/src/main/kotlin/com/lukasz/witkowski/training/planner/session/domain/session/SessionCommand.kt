package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import kotlin.time.Duration

sealed interface SessionCommand {
    sealed interface Exercise : SessionCommand {
        data class UpdateReps(val reps: Int) : Exercise

        data class UpdateWeight(val weightInKg: Double) : Exercise

        data class UpdateHoldDuration(val duration: Duration) : Exercise

        data object StartTimer : Exercise
    }

    sealed interface RestTime : SessionCommand {
        data class UpdateFeedback(val effort: SetEffort) : RestTime
    }

    sealed interface Summary : SessionCommand {
        data class UpdateOverallRpe(val rpe: SessionRpe) : Summary

        data class UpdateNotes(val notes: String) : Summary
    }
}
