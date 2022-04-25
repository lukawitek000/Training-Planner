package com.lukasz.witkowski.shared.utils

import org.junit.Assert.*
import org.junit.Test

class PercentageKtTest {

    @Test
    fun `convert fraction to percentage string`() {
        // given
        val value = 0.2525

        // when
        val percentage = value.toPercentage()

        // then
        assertEquals("25  %", percentage)
    }
}