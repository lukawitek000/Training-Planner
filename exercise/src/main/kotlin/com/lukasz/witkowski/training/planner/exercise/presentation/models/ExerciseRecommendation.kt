package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.shared.utils.Weight
import kotlin.time.Duration

enum class RecommendationLevel(val nameRes: Int) {
    BEGINNER(R.string.beginner),
    INTERMEDIATE(R.string.intermediate),
    ADVANCED(R.string.advanced)
}

data class Recommendation(
    val level: RecommendationLevel,
    val parameters: RecommendedParameters = RecommendedParameters()
)

// TODO rename to use it not just for recommendation
data class RecommendedParameters(
    val sets: Int? = null,
    val reps: Int? = null,
    val restTime: Duration? = null,
    val weight: Weight? = null,
    val time: Duration? = null
) {
    fun areValid() = sets != null && reps != null && restTime != null
}
