package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.domain.ChecksumCalculator
import java.util.zip.Adler32

internal class Adler32ChecksumCalculator : ChecksumCalculator {

    override fun calculate(image: ImageByteArray): Long {
        val checksum = Adler32()
        checksum.update(image.data)
        return checksum.value
    }
}
