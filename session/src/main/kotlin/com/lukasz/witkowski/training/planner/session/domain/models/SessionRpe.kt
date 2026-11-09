package com.lukasz.witkowski.training.planner.session.domain.models

/**
 * 1–10 Rate of Perceived Exertion (RPE) representing internal training load for the entire workout
 * based on the canonical Foster Session-RPE (sRPE) and Borg CR-10 scale.
 */
enum class SessionRpe(val score: Int) {
    VERY_LIGHT(1),
    LIGHT(2),
    MODERATE_LIGHT(3),
    MODERATE(4),
    SOMEWHAT_HARD(5),
    CHALLENGING(6),
    HARD(7),
    VERY_HARD(8),
    NEAR_LIMIT(9),
    MAXIMAL(10);

    companion object {
        fun fromScore(score: Int?): SessionRpe? = entries.firstOrNull { it.score == score }
    }
}
