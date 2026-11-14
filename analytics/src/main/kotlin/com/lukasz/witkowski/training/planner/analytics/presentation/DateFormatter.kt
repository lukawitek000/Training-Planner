package com.lukasz.witkowski.training.planner.analytics.presentation

import android.content.Context
import com.lukasz.witkowski.training.planner.analytics.R
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

internal data class FormattedDateLabel(
    val primary: String,
    val secondary: String
)

internal fun Date.toFormattedDateLabel(
    context: Context,
    now: ZonedDateTime = ZonedDateTime.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): FormattedDateLabel {
    val targetDateTime = this.toInstant().atZone(zoneId)
    val targetDate = targetDateTime.toLocalDate()
    val todayDate = now.toLocalDate()

    val daysBetween = ChronoUnit.DAYS.between(targetDate, todayDate)
    val monthsBetween = ChronoUnit.MONTHS.between(targetDate, todayDate)
    val yearsBetween = ChronoUnit.YEARS.between(targetDate, todayDate)

    val shortDateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm", locale)
    val fullDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)

    return when {
        daysBetween <= 0 -> FormattedDateLabel(
            primary = context.getString(R.string.date_today),
            secondary = targetDateTime.format(shortDateTimeFormatter)
        )

        daysBetween == 1L -> FormattedDateLabel(
            primary = context.getString(R.string.date_yesterday),
            secondary = targetDateTime.format(shortDateTimeFormatter)
        )

        daysBetween in 2..6 -> FormattedDateLabel(
            primary = targetDateTime.format(fullDateFormatter),
            secondary = context.resources.getQuantityString(
                R.plurals.date_days_ago,
                daysBetween.toInt(),
                daysBetween.toInt()
            )
        )

        daysBetween in 7..13 -> FormattedDateLabel(
            primary = targetDateTime.format(fullDateFormatter),
            secondary = context.getString(R.string.date_last_week)
        )

        monthsBetween <= 1L -> FormattedDateLabel(
            primary = targetDateTime.format(fullDateFormatter),
            secondary = context.getString(R.string.date_one_month_ago)
        )

        yearsBetween < 1L -> FormattedDateLabel(
            primary = targetDateTime.format(fullDateFormatter),
            secondary = context.getString(R.string.date_few_months_ago)
        )

        yearsBetween == 1L -> FormattedDateLabel(
            primary = targetDateTime.format(fullDateFormatter),
            secondary = context.getString(R.string.date_last_year)
        )

        else -> FormattedDateLabel(
            primary = targetDateTime.format(fullDateFormatter),
            secondary = context.getString(R.string.date_few_years_ago)
        )
    }
}
