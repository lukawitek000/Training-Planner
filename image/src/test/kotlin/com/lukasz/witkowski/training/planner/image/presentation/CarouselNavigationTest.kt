package com.lukasz.witkowski.training.planner.image.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class CarouselNavigationTest {

    @Test
    fun `next index advances linearly when not at the end`() {
        val next = calculateNextCarouselIndex(currentIndex = 0, totalCount = 3, cycle = false)
        assertEquals(1, next)

        val next2 = calculateNextCarouselIndex(currentIndex = 1, totalCount = 3, cycle = false)
        assertEquals(2, next2)
    }

    @Test
    fun `next index stops at the end when cycle is false`() {
        val next = calculateNextCarouselIndex(currentIndex = 2, totalCount = 3, cycle = false)
        assertEquals(2, next)
    }

    @Test
    fun `next index wraps around to zero when cycle is true`() {
        val next = calculateNextCarouselIndex(currentIndex = 2, totalCount = 3, cycle = true)
        assertEquals(0, next)
    }

    @Test
    fun `previous index steps backward when not at the start`() {
        val prev = calculatePreviousCarouselIndex(currentIndex = 2, totalCount = 3, cycle = false)
        assertEquals(1, prev)

        val prev2 = calculatePreviousCarouselIndex(currentIndex = 1, totalCount = 3, cycle = false)
        assertEquals(0, prev2)
    }

    @Test
    fun `previous index stops at zero when cycle is false`() {
        val prev = calculatePreviousCarouselIndex(currentIndex = 0, totalCount = 3, cycle = false)
        assertEquals(0, prev)
    }

    @Test
    fun `previous index wraps around to last index when cycle is true`() {
        val prev = calculatePreviousCarouselIndex(currentIndex = 0, totalCount = 3, cycle = true)
        assertEquals(2, prev)
    }

    @Test
    fun `returns 0 when totalCount is empty or invalid`() {
        assertEquals(0, calculateNextCarouselIndex(0, 0))
        assertEquals(0, calculatePreviousCarouselIndex(0, 0))
    }
}
