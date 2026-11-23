package com.lukasz.witkowski.training.planner.shared.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.DateFormat as IcuDateFormat
import android.icu.util.TimeZone as IcuTimeZone
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lukasz.witkowski.training.planner.shared.R
import java.time.Instant as JavaInstant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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

fun interface DateTimeFormatEngine {
    fun format(instant: Instant, skeleton: String, zoneId: ZoneId, locale: Locale): String
}

private const val EXTRA_TIMEZONE = "time-zone"

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

/**
 * Uses ICU's [IcuDateFormat.getInstanceForSkeleton] (API 24+), which natively handles Unicode
 * skeletons including `'B'` flexible day-periods, locale calendars, and localized digits.
 * A fresh formatter is created per call because ICU [IcuDateFormat] is not thread-safe.
 */
private val AndroidIcuDateTimeFormatEngine =
    DateTimeFormatEngine { instant, skeleton, zoneId, locale ->
        val formatter = IcuDateFormat.getInstanceForSkeleton(skeleton, locale).apply {
            timeZone = zoneId.toIcuTimeZone()
        }
        formatter.format(Date(instant.toEpochMilliseconds()))
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
 * Formats [this] instant using a locale-appropriate skeleton via [formatEngine].
 */
fun Instant.formatDateTime(
    style: DateTimeStyle,
    formatEngine: DateTimeFormatEngine = AndroidIcuDateTimeFormatEngine,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = true
): String = formatEngine.format(
    instant = this,
    skeleton = style.toSkeleton(is24Hour),
    zoneId = zoneId,
    locale = locale
)

fun Instant.formatDateTime(
    context: Context,
    style: DateTimeStyle,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = DateFormat.is24HourFormat(context)
): String = formatDateTime(
    style = style,
    formatEngine = AndroidIcuDateTimeFormatEngine,
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
    val timeProvider: TimeProvider,
    val now: Instant,
    val todayDate: LocalDate,
    val zoneId: ZoneId,
    val locale: Locale,
    val is24Hour: Boolean
)

/**
 * Provides a single shared [TimeDisplayEnvironment] for the subtree so date/time composables do
 * not register per-item lifecycle observers, broadcast receivers, or midnight timers.
 */
@Composable
fun ProvideTimeDisplayEnvironment(
    timeProvider: TimeProvider = LocalTimeProvider.current,
    content: @Composable () -> Unit
) {
    val env = rememberTimeDisplayEnvironment(timeProvider)
    CompositionLocalProvider(
        LocalTimeProvider provides timeProvider,
        LocalTimeDisplayEnvironment provides env,
        content = content
    )
}

@Composable
internal fun rememberTimeDisplayEnvironment(
    timeProvider: TimeProvider = LocalTimeProvider.current
): TimeDisplayEnvironment {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInspectionMode = LocalInspectionMode.current
    val locale = remember(configuration) {
        configuration.locales[0] ?: Locale.getDefault()
    }

    var zoneId by remember { mutableStateOf(ZoneId.systemDefault()) }
    var is24Hour by remember(context) { mutableStateOf(DateFormat.is24HourFormat(context)) }
    var now by remember(timeProvider) { mutableStateOf(timeProvider.currentInstant()) }

    fun refreshEnvironment(intent: Intent? = null) {
        val explicitZone = intent
            ?.takeIf { it.action == Intent.ACTION_TIMEZONE_CHANGED }
            ?.getStringExtra(EXTRA_TIMEZONE)
            ?.let { zoneStr -> runCatching { ZoneId.of(zoneStr) }.getOrNull() }
        zoneId = explicitZone ?: ZoneId.systemDefault()
        is24Hour = DateFormat.is24HourFormat(context)
        now = timeProvider.currentInstant()
    }

    DisposableEffect(lifecycleOwner, context, timeProvider, isInspectionMode) {
        if (isInspectionMode) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    refreshEnvironment()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    DisposableEffect(context, timeProvider, isInspectionMode) {
        if (isInspectionMode) {
            onDispose { }
        } else {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(receiverContext: Context?, intent: Intent?) {
                    refreshEnvironment(intent)
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, filter)
            }
            onDispose {
                runCatching { context.unregisterReceiver(receiver) }
            }
        }
    }

    val todayDate = remember(now, zoneId) {
        now.toJavaInstant().atZone(zoneId).toLocalDate()
    }

    LaunchedEffect(now, zoneId, timeProvider, isInspectionMode) {
        if (isInspectionMode) return@LaunchedEffect
        while (isActive) {
            val current = timeProvider.currentInstant()
            val currentZoned = ZonedDateTime.ofInstant(current.toJavaInstant(), zoneId)
            val currentLocalDate = currentZoned.toLocalDate()
            val nextMidnight = currentLocalDate.plusDays(1).atStartOfDay(zoneId)
            val millisUntilMidnight =
                (nextMidnight.toInstant().toEpochMilli() - currentZoned.toInstant().toEpochMilli())
                    .coerceAtLeast(1_000L)
            delay(millisUntilMidnight)
            val updated = timeProvider.currentInstant()
            if (updated == current) {
                break
            }
            now = updated
            zoneId = ZoneId.systemDefault()
        }
    }

    return remember(timeProvider, now, todayDate, zoneId, locale, is24Hour) {
        TimeDisplayEnvironment(
            timeProvider = timeProvider,
            now = now,
            todayDate = todayDate,
            zoneId = zoneId,
            locale = locale,
            is24Hour = is24Hour
        )
    }
}

@Composable
private fun currentTimeDisplayEnvironment(): TimeDisplayEnvironment {
    val currentProvider = LocalTimeProvider.current
    return LocalTimeDisplayEnvironment.current
        ?.takeIf { it.timeProvider === currentProvider }
        ?: rememberTimeDisplayEnvironment(currentProvider)
}

@Composable
fun Instant.formatDateTime(style: DateTimeStyle): String {
    val context = LocalContext.current
    val env = currentTimeDisplayEnvironment()
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
    val env = currentTimeDisplayEnvironment()
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
    val env = currentTimeDisplayEnvironment()
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
