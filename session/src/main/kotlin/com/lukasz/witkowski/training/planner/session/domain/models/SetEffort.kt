package com.lukasz.witkowski.training.planner.session.domain.models

/**
 * Subjective difficulty rating for an individual exercise set completed prior to rest.
 * Directly mapped to its corresponding [SessionRpe] level.
 */
enum class SetEffort(val sessionRpe: SessionRpe) {
    EASY(SessionRpe.LIGHT), // RPE 2 (Warmup pace, breathing easily, 5-6 RIR)
    MODERATE(SessionRpe.MODERATE), // RPE 4 (Moderate working set, ~4 RIR)
    HARD(SessionRpe.HARD), // RPE 7 (Demanding heavy set, ~2-3 RIR)
    MAXIMAL(SessionRpe.MAXIMAL); // RPE 10 (Limit / 0 RIR / failure)

    val score: Int get() = sessionRpe.score

    fun toSessionRpe(): SessionRpe = sessionRpe

    companion object {
        fun fromSessionRpe(rpe: SessionRpe): SetEffort = when (rpe) {
            SessionRpe.VERY_LIGHT,
            SessionRpe.LIGHT,
            SessionRpe.MODERATE_LIGHT -> EASY
            SessionRpe.MODERATE,
            SessionRpe.SOMEWHAT_HARD,
            SessionRpe.CHALLENGING -> MODERATE
            SessionRpe.HARD,
            SessionRpe.VERY_HARD -> HARD
            SessionRpe.NEAR_LIMIT,
            SessionRpe.MAXIMAL -> MAXIMAL
        }

        fun fromNumeric(value: Int?): SetEffort? =
            SessionRpe.fromScore(value)?.let(::fromSessionRpe)
    }
}
