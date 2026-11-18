package com.lukasz.witkowski.training.planner.shared.utils

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun `formatUpToTwoDecimals strips trailing zeros and rounds half up`() {
        assertEquals("10", 10.0.formatUpToTwoDecimals(Locale.US))
        assertEquals("62.5", 62.5.formatUpToTwoDecimals(Locale.US))
        assertEquals("2.26", 2.255.formatUpToTwoDecimals(Locale.US))
        assertEquals("132.28", 132.277357.formatUpToTwoDecimals(Locale.US))
    }

    @Test
    fun `formatUpToTwoDecimals respects locale decimal separator`() {
        assertEquals("62,5", 62.5.formatUpToTwoDecimals(Locale.GERMANY))
    }
}
