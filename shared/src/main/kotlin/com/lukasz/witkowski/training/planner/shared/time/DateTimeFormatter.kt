package com.lukasz.witkowski.training.planner.shared.time

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lukasz.witkowski.training.planner.shared.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlinx.coroutines.delay

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

fun interface DatePatternProvider {
    fun bestPattern(locale: Locale, skeleton: String): String
}

private val AndroidDatePatternProvider = DatePatternProvider { locale, skeleton ->
    DateFormat.getBestDateTimePattern(locale, skeleton)
}

internal fun DateTimeStyle.toSkeleton(is24Hour: Boolean): String {
    val timeSkeleton = if (is24Hour) "Hmm" else "hm"
    return when (this) {
        DateTimeStyle.SHORT_DATE_TIME -> "MMMd$timeSkeleton"
        DateTimeStyle.FULL_DATE -> "yMMMd"
        DateTimeStyle.FULL_DATE_TIME -> "yMMMd$timeSkeleton"
    }
}

private fun DateTimeStyle.fallbackFormatter(locale: Locale): DateTimeFormatter = when (this) {
    DateTimeStyle.SHORT_DATE_TIME -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    DateTimeStyle.FULL_DATE -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    DateTimeStyle.FULL_DATE_TIME ->
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
}.withLocale(locale).withDecimalStyle(DecimalStyle.of(locale))

/**
 * Formats [this] instant using a locale-appropriate skeleton pattern resolved via
 * [patternProvider]. Safe across API 28..37 (avoids API 34+ `localizedBy`, API 37+
 * `ofLocalizedPattern`, and API 34+ flexible day-period `'B'` pattern crashes on API 28..33).
 */
fun Instant.formatDateTime(
    style: DateTimeStyle,
    patternProvider: DatePatternProvider = AndroidDatePatternProvider,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = true
): String {
    val rawPattern = patternProvider.bestPattern(locale, style.toSkeleton(is24Hour))
    val sanitizedPattern = rawPattern.replace('B', 'a')
    val formatter = try {
        DateTimeFormatter.ofPattern(sanitizedPattern, locale)
            .withDecimalStyle(DecimalStyle.of(locale))
    } catch (_: IllegalArgumentException) {
        style.fallbackFormatter(locale)
    }
    return toJavaInstant().atZone(zoneId).format(formatter)
}

fun Instant.formatDateTime(
    context: Context,
    style: DateTimeStyle,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = DateFormat.is24HourFormat(context)
): String = formatDateTime(
    style = style,
    patternProvider = AndroidDatePatternProvider,
    zoneId = zoneId,
    locale = locale,
    is24Hour = is24Hour
)

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

@Immutable
internal data class TimeDisplayEnvironment(
    val now: Instant,
    val todayDate: LocalDate,
    val zoneId: ZoneId,
    val locale: Locale,
    val is24Hour: Boolean
)

@Composable
internal fun rememberTimeDisplayEnvironment(
    timeProvider: TimeProvider = LocalTimeProvider.current
): TimeDisplayEnvironment {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val locale = remember(configuration) {
        configuration.locales[0] ?: Locale.getDefault()
    }

    var zoneId by remember { mutableStateOf(ZoneId.systemDefault()) }
    var is24Hour by remember(context) { mutableStateOf(DateFormat.is24HourFormat(context)) }
    var now by remember(timeProvider) { mutableStateOf(timeProvider.currentInstant()) }

    DisposableEffect(lifecycleOwner, context, timeProvider) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                zoneId = ZoneId.systemDefault()
                is24Hour = DateFormat.is24HourFormat(context)
                now = timeProvider.currentInstant()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val todayDate = remember(now, zoneId) {
        now.toJavaInstant().atZone(zoneId).toLocalDate()
    }

    LaunchedEffect(now, zoneId, timeProvider) {
        val currentZoned = ZonedDateTime.ofInstant(now.toJavaInstant(), zoneId)
        val nextMidnight = todayDate.plusDays(1).atStartOfDay(zoneId)
        val millisUntilMidnight =
            (nextMidnight.toInstant().toEpochMilli() - currentZoned.toInstant().toEpochMilli())
                .coerceAtLeast(1_000L)
        delay(millisUntilMidnight)
        val updatedNow = timeProvider.currentInstant()
        if (updatedNow != now) {
            now = updatedNow
        }
    }

    return remember(now, todayDate, zoneId, locale, is24Hour) {
        TimeDisplayEnvironment(
            now = now,
            todayDate = todayDate,
            zoneId = zoneId,
            locale = locale,
            is24Hour = is24Hour
        )
    }
}

@Composable
fun Instant.formatDateTime(style: DateTimeStyle): String {
    val context = LocalContext.current
    val env = rememberTimeDisplayEnvironment()
    return remember(this, context, style, env.zoneId, env.locale, env.is24Hour) {
        formatDateTime(
            context = context,
            style = style,
            zoneId = env.zoneId,
            locale = env.locale,
            is24Hour = env.is24Hour
        )
    }
}

@Composable
fun Instant.toRelativeDateLabel(): RelativeDateLabel {
    val context = LocalContext.current
    val env = rememberTimeDisplayEnvironment()
    return remember(this, context, env.todayDate, env.zoneId, env.locale, env.is24Hour) {
        toRelativeDateLabel(
            context = context,
            now = env.now,
            zoneId = env.zoneId,
            locale = env.locale,
            is24Hour = env.is24Hour
        )
    }
}

@Composable
fun Instant.formatRelativeDate(): String {
    val context = LocalContext.current
    val env = rememberTimeDisplayEnvironment()
    return remember(this, context, env.todayDate, env.zoneId, env.locale, env.is24Hour) {
        formatRelativeDate(
            context = context,
            now = env.now,
            zoneId = env.zoneId,
            locale = env.locale,
            is24Hour = env.is24Hour
        )
    }
}
