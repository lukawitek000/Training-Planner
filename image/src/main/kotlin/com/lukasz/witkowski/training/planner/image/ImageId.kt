package com.lukasz.witkowski.training.planner.image

import java.util.UUID

@JvmInline
value class ImageId(val value: UUID) {

    constructor(name: String) : this(UUID.fromString(name))

    override fun toString() = value.toString()

    companion object {
        fun create(): ImageId = ImageId(UUID.randomUUID())
    }
}
