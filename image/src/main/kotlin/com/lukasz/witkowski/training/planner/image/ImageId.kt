package com.lukasz.witkowski.training.planner.image

import java.util.UUID

@JvmInline
value class ImageId(private val value: UUID) {
    constructor(name: String): this(UUID.fromString(name))

    companion object {
        fun create(): ImageId = ImageId(UUID.randomUUID())
    }

    override fun toString() = value.toString()
}
