package com.lukasz.witkowski.training.planner.shared.time

import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DurationFormatterRobolectricTest {

    @Test
    fun `formats zero and multi-unit durations across styles in English`() {
        assertEquals("0s", Duration.ZERO.formatDuration(DurationFormatStyle.COMPACT, Locale.US))
        assertEquals(
            "1h 30m",
            (1.hours + 30.minutes).formatDuration(DurationFormatStyle.COMPACT, Locale.US)
        )
        assertEquals(
            "1 hr, 30 min",
            (1.hours + 30.minutes).formatDuration(DurationFormatStyle.SHORT, Locale.US)
        )
        assertEquals(
            "1 hour, 30 minutes",
            (1.hours + 30.minutes).formatDuration(DurationFormatStyle.ACCESSIBLE, Locale.US)
        )
    }

    @Test
    fun `formats Polish durations cleanly`() {
        val pl = Locale.forLanguageTag("pl-PL")
        val formatted = (2.minutes + 5.seconds).formatDuration(DurationFormatStyle.ACCESSIBLE, pl)
        assertTrue(formatted.contains("2"))
        assertTrue(formatted.contains("5"))
    }

    @Test
    fun `wraps negative durations with BidiFormatter and LRM for RTL locales`() {
        val ar = Locale.forLanguageTag("ar-EG")
        val arSymbols = DecimalFormatSymbols.getInstance(ar)
        val negativeRtl = (-45).seconds.formatDuration(DurationFormatStyle.COMPACT, ar)
        assertTrue(negativeRtl.contains(arSymbols.minusSign))
        assertTrue(negativeRtl.contains('\u200E'))
        assertTrue(negativeRtl.contains('\u202A'))

        val negativeUs = (-45).seconds.formatDuration(DurationFormatStyle.COMPACT, Locale.US)
        assertEquals("-45s", negativeUs)
        assertFalse(negativeUs.contains('\u200E'))
    }

    @Test
    fun `formats positive and negative infinite durations across LTR and RTL locales`() {
        val positiveInf = Duration.INFINITE.formatDuration(DurationFormatStyle.COMPACT, Locale.US)
        val negativeInf = (-Duration.INFINITE).formatDuration(
            DurationFormatStyle.COMPACT,
            Locale.US
        )
        assertEquals("∞", positiveInf)
        assertEquals("-∞", negativeInf)

        val ar = Locale.forLanguageTag("ar-EG")
        val arSymbols = DecimalFormatSymbols.getInstance(ar)
        val negativeInfRtl = (-Duration.INFINITE).formatDuration(DurationFormatStyle.COMPACT, ar)
        assertTrue(negativeInfRtl.contains(arSymbols.minusSign))
        assertTrue(negativeInfRtl.contains(arSymbols.infinity))
        assertTrue(negativeInfRtl.contains('\u200E'))
        assertTrue(negativeInfRtl.contains('\u202A'))
    }
}
