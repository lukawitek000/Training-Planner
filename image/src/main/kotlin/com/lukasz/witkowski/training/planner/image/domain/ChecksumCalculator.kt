package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.Image
import java.util.zip.Adler32

class ChecksumCalculator {

    fun calculate(image: Image): Long {
        val checksum = Adler32()
        checksum.update(image.data)
        return checksum.value
    }
}
