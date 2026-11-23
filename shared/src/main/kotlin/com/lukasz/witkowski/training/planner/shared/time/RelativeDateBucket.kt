package com.lukasz.witkowski.training.planner.shared.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed interface RelativeDateBucket {
    data object Today : RelativeDateBucket
    data object Yesterday : RelativeDateBucket
    data class DaysAgo(val days: Int) : RelativeDateBucket
    data object LastWeek : RelativeDateBucket
    data class WeeksAgo(val weeks: Int) : RelativeDateBucket
    data object OneMonthAgo : RelativeDateBucket
    data object FewMonthsAgo : RelativeDateBucket
    data object LastYear : RelativeDateBucket
    data object FewYearsAgo : RelativeDateBucket
    data object Future : RelativeDateBucket
}

internal fun classifyRelativeDate(targetDate: LocalDate, todayDate: LocalDate): RelativeDateBucket {
    val daysBetween = ChronoUnit.DAYS.between(targetDate, todayDate)
    val weeksBetween = ChronoUnit.WEEKS.between(targetDate, todayDate).toInt()
    val monthsBetween = ChronoUnit.MONTHS.between(targetDate, todayDate)
    val yearsBetween = ChronoUnit.YEARS.between(targetDate, todayDate)

    return when {
        daysBetween < 0L -> RelativeDateBucket.Future
        daysBetween == 0L -> RelativeDateBucket.Today
        daysBetween == 1L -> RelativeDateBucket.Yesterday
        daysBetween in 2L..6L -> RelativeDateBucket.DaysAgo(daysBetween.toInt())
        daysBetween in 7L..13L -> RelativeDateBucket.LastWeek
        monthsBetween < 1L -> RelativeDateBucket.WeeksAgo(weeksBetween)
        monthsBetween == 1L -> RelativeDateBucket.OneMonthAgo
        yearsBetween < 1L -> RelativeDateBucket.FewMonthsAgo
        yearsBetween == 1L -> RelativeDateBucket.LastYear
        else -> RelativeDateBucket.FewYearsAgo
    }
}
