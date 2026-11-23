package com.lukasz.witkowski.training.planner.exercise.presentation.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class SanitizeDecimalInputTest {

    @Test
    fun `sanitizeDecimalInput normalizes comma and leading zeros`() {
        assertEquals("0.5", sanitizeDecimalInput(".5"))
        assertEquals("0.5", sanitizeDecimalInput("00.5"))
        assertEquals("62.5", sanitizeDecimalInput("62,5"))
        assertEquals("12.34", sanitizeDecimalInput("12.3.4"))
        assertEquals("0", sanitizeDecimalInput("000"))
        assertEquals("5", sanitizeDecimalInput("005"))
        assertEquals("", sanitizeDecimalInput("abc"))
    }
}
