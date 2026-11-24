package com.lukasz.witkowski.training.planner.shared.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Motion and transition constants aligned with Material 3 motion guidelines.
 */
object Motion {
    const val ENTER_DURATION_MS: Int = 300
    const val EXIT_DURATION_MS: Int = 200
    const val FADE_THROUGH_OUT_DURATION_MS: Int = 90
    const val FADE_THROUGH_IN_DURATION_MS: Int = 210
    const val FADE_THROUGH_DELAY_MS: Int = 90

    const val FADE_THROUGH_INITIAL_SCALE: Float = 0.92f
    const val SESSION_SCALE_INITIAL: Float = 0.92f

    /** Standard Material shared-axis travel distance - 30dp. */
    val sharedAxisOffset: Dp = 30.dp
}
