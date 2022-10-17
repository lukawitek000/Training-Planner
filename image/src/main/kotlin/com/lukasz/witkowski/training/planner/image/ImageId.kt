package com.lukasz.witkowski.training.planner.image

import java.util.UUID

@JvmInline
value class ImageId(val value: String) {
    companion object {
        fun create(): ImageId = ImageId(UUID.randomUUID().toString())
    }
}
