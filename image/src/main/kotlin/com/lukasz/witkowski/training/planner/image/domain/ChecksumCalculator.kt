package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageByteArray

internal interface ChecksumCalculator {
    fun calculate(image: ImageByteArray): Long
}
