package com.lukasz.witkowski.training.planner.shared.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightTest {

    @Test
    fun `ofKilograms converts accurately to pounds`() {
        val weight = 100.0.kg
        assertEquals(100.0, weight.inKilograms, 1e-9)
        assertEquals(220.462262, weight.inPounds, 1e-5)
    }

    @Test
    fun `ofPounds converts accurately to kilograms and round-trips`() {
        val weight = 100.lbs
        assertEquals(45.359237, weight.inKilograms, 1e-9)
        assertEquals(100.0, weight.inPounds, 1e-9)
        assertEquals(100, weight.toInt(WeightUnit.LBS))
    }

    @Test
    fun `toDouble returns value in requested unit`() {
        val kgWeight = Weight.of(60.0, WeightUnit.KG)
        assertEquals(60.0, kgWeight.toDouble(WeightUnit.KG), 1e-9)
        assertEquals(132.277357, kgWeight.toDouble(WeightUnit.LBS), 1e-5)

        val lbsWeight = Weight.of(135.0, WeightUnit.LBS)
        assertEquals(135.0, lbsWeight.toDouble(WeightUnit.LBS), 1e-9)
        assertEquals(61.234969, lbsWeight.toDouble(WeightUnit.KG), 1e-5)
    }

    @Test
    fun `toInt rounds to nearest integer in requested unit`() {
        assertEquals(220, 100.kg.toInt(WeightUnit.LBS))
        assertEquals(45, 100.lbs.toInt(WeightUnit.KG))
        assertEquals(0, Weight.ZERO.toInt(WeightUnit.KG))
    }

    @Test
    fun `compareTo compares weights regardless of construction unit`() {
        assertTrue(50.kg > 100.lbs)
        assertTrue(40.kg < 100.lbs)
        assertEquals(0, Weight.ofKilograms(Weight.LBS_TO_KG).compareTo(1.lbs))
    }

    @Test
    fun `arithmetic operators and isPositive work accurately`() {
        val sum = 20.kg + 10.kg
        assertEquals(30.0, sum.inKilograms, 1e-9)

        val diff = 30.kg - 12.5.kg
        assertEquals(17.5, diff.inKilograms, 1e-9)

        val intScaled = 50.kg * 10
        assertEquals(500.0, intScaled.inKilograms, 1e-9)

        val doubleScaled = 50.kg * 2.5
        assertEquals(125.0, doubleScaled.inKilograms, 1e-9)

        assertTrue(0.1.kg.isPositive)
        assertFalse(Weight.ZERO.isPositive)
        assertFalse((-5.0).kg.isPositive)
    }

    @Test
    fun `defaultStep returns standard gym increment per unit`() {
        assertEquals(2.5, WeightUnit.KG.defaultStep, 1e-9)
        assertEquals(5.0, WeightUnit.LBS.defaultStep, 1e-9)
    }
}
