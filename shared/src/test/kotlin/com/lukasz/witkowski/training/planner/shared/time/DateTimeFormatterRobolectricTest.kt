package com.lukasz.witkowski.training.planner.shared.time

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DateTimeFormatterRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val utc: ZoneId = ZoneId.of("UTC")

    @Test
    fun `formats ICU skeleton patterns across locales and 12h or 24h without B-symbol crashes`() {
        val instant = Instant.parse("2026-11-20T19:23:00Z")
        val locales = listOf(
            Locale.US,
            Locale.forLanguageTag("pl-PL"),
            Locale.forLanguageTag("ar-EG"),
            Locale.forLanguageTag("my-MM"),
            Locale.SIMPLIFIED_CHINESE
        )

        locales.forEach { locale ->
            DateTimeStyle.entries.forEach { style ->
                listOf(true, false).forEach { is24Hour ->
                    val formatted = instant.formatDateTime(
                        context = context,
                        style = style,
                        zoneId = utc,
                        locale = locale,
                        is24Hour = is24Hour
                    )
                    assertTrue(
                        "Blank output for $locale / $style / $is24Hour",
                        formatted.isNotBlank()
                    )
                }
            }
        }
    }

    @Test
    fun `respects 12h vs 24h setting in English`() {
        val instant = Instant.parse("2026-11-20T19:23:00Z")

        val format24 = instant.formatDateTime(
            context = context,
            style = DateTimeStyle.SHORT_DATE_TIME,
            zoneId = utc,
            locale = Locale.US,
            is24Hour = true
        )
        val format12 = instant.formatDateTime(
            context = context,
            style = DateTimeStyle.SHORT_DATE_TIME,
            zoneId = utc,
            locale = Locale.US,
            is24Hour = false
        )

        assertTrue(format24.contains("19:23"))
        assertTrue(format12.contains("7:23"))
        assertFalse(format12.contains("19:23"))
    }

    @Test
    fun `formats fixed-offset ZoneId and UTC+2 without falling back to GMT`() {
        val instant = Instant.parse("2026-11-20T19:23:00Z")

        val offsetFormatted = instant.formatDateTime(
            context = context,
            style = DateTimeStyle.SHORT_DATE_TIME,
            zoneId = ZoneOffset.ofHours(2),
            locale = Locale.US,
            is24Hour = true
        )
        val utcPlusTwoFormatted = instant.formatDateTime(
            context = context,
            style = DateTimeStyle.SHORT_DATE_TIME,
            zoneId = ZoneId.of("UTC+2"),
            locale = Locale.US,
            is24Hour = true
        )

        assertTrue("Expected 21:23 but got $offsetFormatted", offsetFormatted.contains("21:23"))
        assertTrue(
            "Expected 21:23 but got $utcPlusTwoFormatted",
            utcPlusTwoFormatted.contains("21:23")
        )
    }

    @Test
    fun `toRelativeDateLabel and formatRelativeDate cover all buckets`() {
        val now = Instant.parse("2026-11-20T15:00:00Z")

        val future = (now + 2.days).toRelativeDateLabel(context, now, utc, Locale.US, true)
        assertTrue(future.primary.contains("2026"))
        assertEquals("", future.secondary)
        assertTrue(
            (now + 2.days).formatRelativeDate(context, now, utc, Locale.US, true).contains("2026")
        )

        val todayLabel = (now - 1.hours).toRelativeDateLabel(context, now, utc, Locale.US, true)
        assertEquals("Today", todayLabel.primary)
        assertTrue(todayLabel.secondary.contains("14:00"))
        assertEquals(
            "Today",
            (now - 1.hours).formatRelativeDate(context, now, utc, Locale.US, true)
        )

        val yesterdayLabel = (now - 1.days).toRelativeDateLabel(context, now, utc, Locale.US, true)
        assertEquals("Yesterday", yesterdayLabel.primary)
        assertEquals(
            "Yesterday",
            (now - 1.days).formatRelativeDate(context, now, utc, Locale.US, true)
        )

        assertEquals(
            "3 days ago",
            (now - 3.days).toRelativeDateLabel(context, now, utc, Locale.US, true).secondary
        )
        assertEquals(
            "Last week",
            (now - 8.days).toRelativeDateLabel(context, now, utc, Locale.US, true).secondary
        )
        assertEquals(
            "2 weeks ago",
            (now - 15.days).toRelativeDateLabel(context, now, utc, Locale.US, true).secondary
        )
        assertEquals(
            "1 month ago",
            Instant.parse("2026-10-15T12:00:00Z")
                .toRelativeDateLabel(context, now, utc, Locale.US, true)
                .secondary
        )
        assertEquals(
            "Few months ago",
            Instant.parse("2026-06-15T12:00:00Z")
                .toRelativeDateLabel(context, now, utc, Locale.US, true)
                .secondary
        )
        assertEquals(
            "Last year",
            Instant.parse("2025-10-15T12:00:00Z")
                .toRelativeDateLabel(context, now, utc, Locale.US, true)
                .secondary
        )
        assertEquals(
            "Few years ago",
            Instant.parse("2024-05-15T12:00:00Z")
                .toRelativeDateLabel(context, now, utc, Locale.US, true)
                .secondary
        )
    }

    @Test
    fun `composable relative formatters use LocalTimeProvider`() {
        val targetInstant = Instant.parse("2026-11-20T12:00:00Z")
        val timeProvider = TestTimeProvider(instant = targetInstant + 1.days)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalTimeProvider provides timeProvider) {
                Text(text = targetInstant.formatRelativeDate())
                Text(text = targetInstant.toRelativeDateLabel().primary)
            }
        }

        composeTestRule.onAllNodesWithText("Yesterday").assertCountEquals(2)
    }
}
