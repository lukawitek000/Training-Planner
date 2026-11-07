package com.lukasz.witkowski.training.planner.session.domain.session

import kotlin.time.Duration

sealed interface SessionCommand {
    sealed interface Exercise : SessionCommand {
        data class UpdateReps(val reps: Int) : Exercise

        data class UpdateWeight(val weightInKg: Double) : Exercise

        data class UpdateHoldDuration(val duration: Duration) : Exercise

        data object StartTimer : Exercise
    }

    sealed interface RestTime : SessionCommand {
        data class UpdateRpeFeedback(val rpe: Int?) : RestTime
    }

    sealed interface Summary : SessionCommand {
        data class UpdateOverallRpe(val rpe: Int) : Summary

        data class UpdateNotes(val notes: String) : Summary
    }
}
