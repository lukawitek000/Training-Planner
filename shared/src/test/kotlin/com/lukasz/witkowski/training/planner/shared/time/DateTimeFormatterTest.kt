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
    fun `month end and leap year boundaries follow calendar ChronoUnit rules`() {
        // Jan 31 -> Feb 28 is 28 days (< 1 full calendar month), so it is 4 weeks ago
        assertEquals(
            RelativeDateBucket.WeeksAgo(4),
            classifyRelativeDate(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28))
        )
        // Jan 28 -> Feb 28 is 1 full calendar month
        assertEquals(
            RelativeDateBucket.OneMonthAgo,
            classifyRelativeDate(LocalDate.of(2026, 1, 28), LocalDate.of(2026, 2, 28))
        )
        // Leap day 2024-02-29 -> 2025-02-28 is 11 months (1 day short of 1 full year)
        assertEquals(
            RelativeDateBucket.FewMonthsAgo,
            classifyRelativeDate(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28))
        )
        // Leap day 2024-02-29 -> 2025-03-01 is >= 1 full calendar year
        assertEquals(
            RelativeDateBucket.LastYear,
            classifyRelativeDate(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 3, 1))
        )
    }

    @Test
    fun `skeleton respects 24h vs 12h preference`() {
        assertEquals("MMMdHmm", DateTimeStyle.SHORT_DATE_TIME.toSkeleton(is24Hour = true))
        assertEquals("MMMdhmm", DateTimeStyle.SHORT_DATE_TIME.toSkeleton(is24Hour = false))
        assertEquals("yMMMd", DateTimeStyle.FULL_DATE.toSkeleton(is24Hour = true))
        assertEquals("yMMMd", DateTimeStyle.FULL_DATE.toSkeleton(is24Hour = false))
        assertEquals("yMMMdHmm", DateTimeStyle.FULL_DATE_TIME.toSkeleton(is24Hour = true))
        assertEquals("yMMMdhmm", DateTimeStyle.FULL_DATE_TIME.toSkeleton(is24Hour = false))
    }

    @Test
    fun `formatDateTime delegates skeleton, zone, and locale to injected engine`() {
        val instant = Instant.parse("2026-11-20T19:23:00Z")
        val engine = DateTimeFormatEngine { _, skeleton, zoneId, locale ->
            "$skeleton@${zoneId.id}@${locale.toLanguageTag()}"
        }
        val utc = ZoneId.of("UTC")

        assertEquals(
            "MMMdHmm@UTC@en-US",
            instant.formatDateTime(
                style = DateTimeStyle.SHORT_DATE_TIME,
                formatEngine = engine,
                zoneId = utc,
                locale = Locale.US,
                is24Hour = true
            )
        )
        assertEquals(
            "MMMdhmm@UTC@en-US",
            instant.formatDateTime(
                style = DateTimeStyle.SHORT_DATE_TIME,
                formatEngine = engine,
                zoneId = utc,
                locale = Locale.US,
                is24Hour = false
            )
        )
        assertEquals(
            "yMMMd@UTC@pl-PL",
            instant.formatDateTime(
                style = DateTimeStyle.FULL_DATE,
                formatEngine = engine,
                zoneId = utc,
                locale = Locale.forLanguageTag("pl-PL")
            )
        )
    }
}
