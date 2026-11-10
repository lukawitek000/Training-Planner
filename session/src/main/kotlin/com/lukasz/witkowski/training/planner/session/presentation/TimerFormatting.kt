package com.lukasz.witkowski.training.planner.session.presentation

import kotlin.time.Duration

fun formatRemainingTime(time: Duration): String {
    val totalSeconds = time.absoluteValue.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}