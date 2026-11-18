package com.lukasz.witkowski.training.planner.analytics.presentation

import android.content.Context
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DateFormatterTest {
    private val context: Context by lazy { RuntimeEnvironment.getApplication() }
    private val zoneId: ZoneId = ZoneId.of("UTC")
    private val locale: Locale = Locale.US
    private val now: ZonedDateTime = ZonedDateTime.of(2026, 11, 17, 18, 0, 0, 0, zoneId)

    @Test
    fun `today formats as Today with short date time`() {
        val date = Date.from(now.minusHours(3).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Today", label.primary)
        assertEquals("Nov 17, 15:00", label.secondary)
    }

    @Test
    fun `yesterday formats as Yesterday with short date time`() {
        val date = Date.from(now.minusDays(1).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Yesterday", label.primary)
        assertEquals("Nov 16, 18:00", label.secondary)
    }

    @Test
    fun `2 to 6 days ago formats with days ago plural`() {
        val date = Date.from(now.minusDays(4).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Nov 13, 2026", label.primary)
        assertEquals("4 days ago", label.secondary)
    }

    @Test
    fun `7 to 13 days ago formats as Last week`() {
        val date = Date.from(now.minusDays(10).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Nov 7, 2026", label.primary)
        assertEquals("Last week", label.secondary)
    }

    @Test
    fun `14 to 30 days ago formats as weeks ago instead of 1 month ago`() {
        val date = Date.from(now.minusDays(14).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Nov 3, 2026", label.primary)
        assertEquals("2 weeks ago", label.secondary)
    }

    @Test
    fun `1 month ago formats as 1 month ago`() {
        val date = Date.from(now.minusMonths(1).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Oct 17, 2026", label.primary)
        assertEquals("1 month ago", label.secondary)
    }

    @Test
    fun `multiple months ago formats as Few months ago`() {
        val date = Date.from(now.minusMonths(3).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Aug 17, 2026", label.primary)
        assertEquals("Few months ago", label.secondary)
    }

    @Test
    fun `1 year ago formats as Last year`() {
        val date = Date.from(now.minusYears(1).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Nov 17, 2025", label.primary)
        assertEquals("Last year", label.secondary)
    }

    @Test
    fun `multiple years ago formats as Few years ago`() {
        val date = Date.from(now.minusYears(2).toInstant())
        val label = date.toFormattedDateLabel(context, now = now, zoneId = zoneId, locale = locale)
        assertEquals("Nov 17, 2024", label.primary)
        assertEquals("Few years ago", label.secondary)
    }

    @Test
    fun `now is converted to target zoneId before computing local date difference`() {
        val utcNow = ZonedDateTime.of(2026, 11, 17, 23, 30, 0, 0, ZoneId.of("UTC"))
        val targetZone = ZoneId.of("Europe/Warsaw") // +01:00 -> Nov 18, 00:30
        val sessionDate = Date.from(
            ZonedDateTime.of(2026, 11, 17, 10, 0, 0, 0, targetZone).toInstant()
        )
        val label = sessionDate.toFormattedDateLabel(
            context = context,
            now = utcNow,
            zoneId = targetZone,
            locale = locale
        )
        assertEquals("Yesterday", label.primary)
    }
}
