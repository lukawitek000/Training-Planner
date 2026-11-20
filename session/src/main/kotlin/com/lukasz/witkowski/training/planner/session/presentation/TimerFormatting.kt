package com.lukasz.witkowski.training.planner.session.presentation

import java.util.Locale
import kotlin.time.Duration

fun formatRemainingTime(time: Duration, locale: Locale = Locale.getDefault()): String =
    time.absoluteValue.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0L) {
            String.format(locale, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(locale, "%02d:%02d", minutes, seconds)
        }
    }
