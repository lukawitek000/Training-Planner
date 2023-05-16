package com.lukasz.witkowski.training.planner.shared.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PercentageKtTest {

    @Test
    fun `convert fraction to percentage string with floor round`() {
        // given
        val value = 0.2525

        // when
        val percentage = value.toPercentage()

        // then
        assertEquals("25%", percentage)
    }

    @Test
    fun `convert fraction to percentage string with ceil round`() {
        // given
        val value = 0.469

        // when
        val percentage = value.toPercentage()

        // then
        assertEquals("47%", percentage)
    }

    @Test
    fun `convert fraction to percentage string without rounding`() {
        // given
        val value = 0.12

        // when
        val percentage = value.toPercentage()

        // then
        assertEquals("12%", percentage)
    }

    @Test
    fun `convert fraction with one decimal to percentage string`() {
        // given
        val value = 0.6

        // when
        val percentage = value.toPercentage()

        // then
        assertEquals("60%", percentage)
    }

    @Test
    fun `convert 1 to percentage string`() {
        // given
        val value = 1.0

        // when
        val percentage = value.toPercentage()

        // then
        assertEquals("100%", percentage)
    }

    @Test
    fun `convert 0 to percentage string`() {
        // given
        val value = 0.0

        // when
        val percentage = value.toPercentage()

        // then
        assertEquals("0%", percentage)
    }

    @Test
    fun `negative number to percentage string throws exception`() {
        // given
        val value = -50.0

        // then
        assertThrows(IllegalArgumentException::class.java) {
            // when
            value.toPercentage()
        }
    }
}
