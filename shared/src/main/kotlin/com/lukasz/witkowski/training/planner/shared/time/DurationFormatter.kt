package com.lukasz.witkowski.training.planner.shared.time

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

enum class DurationFormatStyle {
    /** Compact UI chips, bento tiles, and list summaries (e.g., "1h 30m", "45s"). */
    COMPACT,

    /** Abbreviated prose labels (e.g., "1 hr, 30 mins", "45 secs"). */
    SHORT,

    /** Full words suitable for accessibility / TalkBack (e.g., "1 hour, 30 minutes"). */
    ACCESSIBLE
}

internal enum class DurationUnitPart {
    HOURS,
    MINUTES,
    SECONDS
}

internal data class DurationPart(val value: Long, val unit: DurationUnitPart)

internal data class DurationDisplayComponents(
    val isNegative: Boolean,
    val parts: List<DurationPart>
)

/**
 * Rounds [this] duration to the nearest whole second *before* decomposing into hours, minutes,
 * and seconds, preventing overflow artifacts such as "0m 60s" or "1h 60m".
 *
 * Zero-valued components are omitted unless the entire rounded duration is zero, in which case
 * a single `0 SECONDS` part is returned.
 */
internal fun Duration.decomposeForDisplay(): DurationDisplayComponents {
    val roundedSeconds = absoluteValue.toDouble(DurationUnit.SECONDS).roundToLong()
    val isEffectivelyNegative = isNegative() && roundedSeconds > 0L
    val parts = roundedSeconds.seconds.toComponents { hours, minutes, seconds, _ ->
        buildList {
            if (hours > 0L) add(DurationPart(hours, DurationUnitPart.HOURS))
            if (minutes > 0) add(DurationPart(minutes.toLong(), DurationUnitPart.MINUTES))
            if (seconds > 0) add(DurationPart(seconds.toLong(), DurationUnitPart.SECONDS))
            if (isEmpty()) add(DurationPart(0L, DurationUnitPart.SECONDS))
        }
    }
    return DurationDisplayComponents(
        isNegative = isEffectivelyNegative,
        parts = parts
    )
}

/**
 * Formats [this] duration into a locale-aware, RTL-safe string using ICU [MeasureFormat].
 */
fun Duration.formatDuration(
    style: DurationFormatStyle = DurationFormatStyle.COMPACT,
    locale: Locale = Locale.getDefault()
): String {
    val symbols = DecimalFormatSymbols.getInstance(locale)
    if (!isFinite()) {
        return if (isNegative()) {
            "${symbols.minusSign}${symbols.infinity}"
        } else {
            symbols.infinity
        }
    }
    val components = decomposeForDisplay()
    val measures = components.parts.map { part ->
        Measure(part.value, part.unit.toMeasureUnit())
    }.toTypedArray()
    val formatted = MeasureFormat.getInstance(locale, style.toFormatWidth())
        .formatMeasures(*measures)
    return if (components.isNegative) {
        "${symbols.minusSign}$formatted"
    } else {
        formatted
    }
}

/**
 * Composable wrapper around [Duration.formatDuration] that automatically reacts to runtime
 * locale changes via [LocalConfiguration].
 */
@Composable
fun Duration.formatDuration(style: DurationFormatStyle = DurationFormatStyle.COMPACT): String {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        configuration.locales[0] ?: Locale.getDefault()
    }
    return remember(this, style, locale) {
        formatDuration(style = style, locale = locale)
    }
}

private fun DurationFormatStyle.toFormatWidth(): MeasureFormat.FormatWidth = when (this) {
    DurationFormatStyle.COMPACT -> MeasureFormat.FormatWidth.NARROW
    DurationFormatStyle.SHORT -> MeasureFormat.FormatWidth.SHORT
    DurationFormatStyle.ACCESSIBLE -> MeasureFormat.FormatWidth.WIDE
}

private fun DurationUnitPart.toMeasureUnit(): MeasureUnit = when (this) {
    DurationUnitPart.HOURS -> MeasureUnit.HOUR
    DurationUnitPart.MINUTES -> MeasureUnit.MINUTE
    DurationUnitPart.SECONDS -> MeasureUnit.SECOND
}
