package com.lukasz.witkowski.training.planner.exercise.domain

import java.util.UUID

@JvmInline
value class ImageId(val value: String) {
    companion object {
        fun create(): ImageId = ImageId(UUID.randomUUID().toString())
    }
}
