package com.lukasz.witkowski.training.planner.shared.time

import android.content.Context
import android.icu.text.DateFormat as IcuDateFormat
import android.icu.util.TimeZone as IcuTimeZone
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.lukasz.witkowski.training.planner.shared.R
import java.time.Instant as JavaInstant
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

enum class DateTimeStyle {
    /** Localized month/day + time (e.g., "Nov 20, 20:23" or "Nov 20, 8:23 PM"). */
    SHORT_DATE_TIME,

    /** Localized year/month/day (e.g., "Nov 20, 2026" or "20 lis 2026"). */
    FULL_DATE,

    /** Localized year/month/day + time (e.g., "Nov 20, 2026, 20:23"). */
    FULL_DATE_TIME
}

@Immutable
data class RelativeDateLabel(val primary: String, val secondary: String)

private fun ZoneId.toIcuTimeZone(): IcuTimeZone {
    if (rules.isFixedOffset) {
        val offset = rules.getOffset(JavaInstant.EPOCH)
        if (offset.totalSeconds == 0) {
            return IcuTimeZone.getTimeZone("GMT")
        }
        return IcuTimeZone.getTimeZone("GMT${offset.id}")
    }
    return IcuTimeZone.getTimeZone(id)
}

internal fun DateTimeStyle.toSkeleton(is24Hour: Boolean): String {
    val timeSkeleton = if (is24Hour) "Hmm" else "hmm"
    return when (this) {
        DateTimeStyle.SHORT_DATE_TIME -> "MMMd$timeSkeleton"
        DateTimeStyle.FULL_DATE -> "yMMMd"
        DateTimeStyle.FULL_DATE_TIME -> "yMMMd$timeSkeleton"
    }
}

/**
 * Formats [this] instant using a locale-appropriate ICU skeleton (API 24+).
 * A fresh formatter is created per call because ICU [IcuDateFormat] is not thread-safe.
 */
fun Instant.formatDateTime(
    context: Context,
    style: DateTimeStyle,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = DateFormat.is24HourFormat(context)
): String {
    val formatter = IcuDateFormat.getInstanceForSkeleton(style.toSkeleton(is24Hour), locale).apply {
        timeZone = zoneId.toIcuTimeZone()
    }
    return formatter.format(Date(toEpochMilliseconds()))
}

private fun RelativeDateBucket.toLocalizedRelativeText(context: Context): String? = when (this) {
    RelativeDateBucket.Future -> null
    RelativeDateBucket.Today -> context.getString(R.string.date_today)
    RelativeDateBucket.Yesterday -> context.getString(R.string.date_yesterday)
    is RelativeDateBucket.DaysAgo -> context.resources.getQuantityString(
        R.plurals.date_days_ago,
        days,
        days
    )

    RelativeDateBucket.LastWeek -> context.getString(R.string.date_last_week)
    is RelativeDateBucket.WeeksAgo -> context.resources.getQuantityString(
        R.plurals.date_weeks_ago,
        weeks,
        weeks
    )

    RelativeDateBucket.OneMonthAgo -> context.getString(R.string.date_one_month_ago)
    RelativeDateBucket.FewMonthsAgo -> context.getString(R.string.date_few_months_ago)
    RelativeDateBucket.LastYear -> context.getString(R.string.date_last_year)
    RelativeDateBucket.FewYearsAgo -> context.getString(R.string.date_few_years_ago)
}

fun Instant.toRelativeDateLabel(
    context: Context,
    now: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = DateFormat.is24HourFormat(context)
): RelativeDateLabel {
    val targetDate = toJavaInstant().atZone(zoneId).toLocalDate()
    val todayDate = now.toJavaInstant().atZone(zoneId).toLocalDate()
    val bucket = classifyRelativeDate(targetDate = targetDate, todayDate = todayDate)

    return when (bucket) {
        RelativeDateBucket.Future -> RelativeDateLabel(
            primary = formatDateTime(
                context,
                DateTimeStyle.FULL_DATE_TIME,
                zoneId,
                locale,
                is24Hour
            ),
            secondary = ""
        )

        RelativeDateBucket.Today, RelativeDateBucket.Yesterday -> RelativeDateLabel(
            primary = bucket.toLocalizedRelativeText(context).orEmpty(),
            secondary = formatDateTime(
                context,
                DateTimeStyle.SHORT_DATE_TIME,
                zoneId,
                locale,
                is24Hour
            )
        )

        else -> RelativeDateLabel(
            primary = formatDateTime(context, DateTimeStyle.FULL_DATE, zoneId, locale, is24Hour),
            secondary = bucket.toLocalizedRelativeText(context).orEmpty()
        )
    }
}

fun Instant.formatRelativeDate(
    context: Context,
    now: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = DateFormat.is24HourFormat(context)
): String {
    val targetDate = toJavaInstant().atZone(zoneId).toLocalDate()
    val todayDate = now.toJavaInstant().atZone(zoneId).toLocalDate()
    val bucket = classifyRelativeDate(targetDate = targetDate, todayDate = todayDate)
    return bucket.toLocalizedRelativeText(context)
        ?: formatDateTime(context, DateTimeStyle.FULL_DATE, zoneId, locale, is24Hour)
}

@Composable
fun Instant.formatDateTime(style: DateTimeStyle): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(this, context, configuration, style) {
        formatDateTime(
            context = context,
            style = style,
            locale = configuration.locales[0] ?: Locale.getDefault()
        )
    }
}

@Composable
fun Instant.toRelativeDateLabel(): RelativeDateLabel {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val timeProvider = LocalTimeProvider.current
    return remember(this, context, configuration, timeProvider) {
        toRelativeDateLabel(
            context = context,
            now = timeProvider.currentInstant(),
            locale = configuration.locales[0] ?: Locale.getDefault()
        )
    }
}

@Composable
fun Instant.formatRelativeDate(): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val timeProvider = LocalTimeProvider.current
    return remember(this, context, configuration, timeProvider) {
        formatRelativeDate(
            context = context,
            now = timeProvider.currentInstant(),
            locale = configuration.locales[0] ?: Locale.getDefault()
        )
    }
}
