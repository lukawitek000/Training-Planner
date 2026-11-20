package com.lukasz.witkowski.training.planner.shared.time

import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeFormatterTest {

    private val today = LocalDate.of(2026, 11, 20)

    @Test
    fun `future date classifies as Future`() {
        assertEquals(
            RelativeDateBucket.Future,
            classifyRelativeDate(today.plusDays(1), today)
        )
    }

    @Test
    fun `same calendar day classifies as Today`() {
        assertEquals(
            RelativeDateBucket.Today,
            classifyRelativeDate(today, today)
        )
    }

    @Test
    fun `previous calendar day classifies as Yesterday`() {
        assertEquals(
            RelativeDateBucket.Yesterday,
            classifyRelativeDate(today.minusDays(1), today)
        )
    }

    @Test
    fun `2 to 6 days ago classifies as DaysAgo`() {
        assertEquals(
            RelativeDateBucket.DaysAgo(2),
            classifyRelativeDate(today.minusDays(2), today)
        )
        assertEquals(
            RelativeDateBucket.DaysAgo(6),
            classifyRelativeDate(today.minusDays(6), today)
        )
    }

    @Test
    fun `7 to 13 days ago classifies as LastWeek`() {
        assertEquals(
            RelativeDateBucket.LastWeek,
            classifyRelativeDate(today.minusDays(7), today)
        )
        assertEquals(
            RelativeDateBucket.LastWeek,
            classifyRelativeDate(today.minusDays(13), today)
        )
    }

    @Test
    fun `14 days to under 1 month classifies as WeeksAgo`() {
        assertEquals(
            RelativeDateBucket.WeeksAgo(2),
            classifyRelativeDate(today.minusDays(14), today)
        )
        assertEquals(
            RelativeDateBucket.WeeksAgo(3),
            classifyRelativeDate(today.minusDays(25), today)
        )
    }

    @Test
    fun `1 month ago classifies as OneMonthAgo`() {
        assertEquals(
            RelativeDateBucket.OneMonthAgo,
            classifyRelativeDate(today.minusMonths(1), today)
        )
    }

    @Test
    fun `2 to 11 months ago classifies as FewMonthsAgo`() {
        assertEquals(
            RelativeDateBucket.FewMonthsAgo,
            classifyRelativeDate(today.minusMonths(2), today)
        )
        assertEquals(
            RelativeDateBucket.FewMonthsAgo,
            classifyRelativeDate(today.minusMonths(11), today)
        )
    }

    @Test
    fun `1 year ago classifies as LastYear`() {
        assertEquals(
            RelativeDateBucket.LastYear,
            classifyRelativeDate(today.minusYears(1), today)
        )
    }

    @Test
    fun `2 or more years ago classifies as FewYearsAgo`() {
        assertEquals(
            RelativeDateBucket.FewYearsAgo,
            classifyRelativeDate(today.minusYears(2), today)
        )
    }

    @Test
    fun `skeleton respects 24h vs 12h preference`() {
        assertEquals("MMMdHmm", DateTimeStyle.SHORT_DATE_TIME.toSkeleton(is24Hour = true))
        assertEquals("MMMdhm", DateTimeStyle.SHORT_DATE_TIME.toSkeleton(is24Hour = false))
        assertEquals("yMMMd", DateTimeStyle.FULL_DATE.toSkeleton(is24Hour = true))
        assertEquals("yMMMd", DateTimeStyle.FULL_DATE.toSkeleton(is24Hour = false))
        assertEquals("yMMMdHmm", DateTimeStyle.FULL_DATE_TIME.toSkeleton(is24Hour = true))
        assertEquals("yMMMdhm", DateTimeStyle.FULL_DATE_TIME.toSkeleton(is24Hour = false))
    }

    @Test
    fun `formatDateTime formats instant with injected pattern provider and timezone`() {
        val instant = Instant.parse("2026-11-20T19:23:00Z")
        val provider = DatePatternProvider { _, skeleton ->
            when (skeleton) {
                "MMMdHmm" -> "MMM d, HH:mm"
                "MMMdhm" -> "MMM d, h:mm a"
                "yMMMd" -> "MMM d, yyyy"
                else -> "MMM d, yyyy, HH:mm"
            }
        }
        val utc = ZoneId.of("UTC")

        assertEquals(
            "Nov 20, 19:23",
            instant.formatDateTime(
                style = DateTimeStyle.SHORT_DATE_TIME,
                patternProvider = provider,
                zoneId = utc,
                locale = Locale.US,
                is24Hour = true
            )
        )
        assertEquals(
            "Nov 20, 7:23 PM",
            instant.formatDateTime(
                style = DateTimeStyle.SHORT_DATE_TIME,
                patternProvider = provider,
                zoneId = utc,
                locale = Locale.US,
                is24Hour = false
            )
        )
        assertEquals(
            "Nov 20, 2026",
            instant.formatDateTime(
                style = DateTimeStyle.FULL_DATE,
                patternProvider = provider,
                zoneId = utc,
                locale = Locale.US
            )
        )
    }

    @Test
    fun `formatDateTime sanitizes ICU B day-period symbol and falls back on invalid pattern`() {
        val instant = Instant.parse("2026-11-20T19:23:00Z")
        val utc = ZoneId.of("UTC")

        val sanitized = instant.formatDateTime(
            style = DateTimeStyle.SHORT_DATE_TIME,
            patternProvider = DatePatternProvider { _, _ -> "MMM d, h:mm B" },
            zoneId = utc,
            locale = Locale.US,
            is24Hour = false
        )
        assertEquals("Nov 20, 7:23 PM", sanitized)

        val fallback = instant.formatDateTime(
            style = DateTimeStyle.FULL_DATE,
            patternProvider = DatePatternProvider { _, _ -> "invalid pattern %%%" },
            zoneId = utc,
            locale = Locale.US
        )
        assertEquals("Nov 20, 2026", fallback)
    }
}
