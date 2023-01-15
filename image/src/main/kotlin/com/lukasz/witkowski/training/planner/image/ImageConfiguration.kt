package com.lukasz.witkowski.training.planner.image

import java.util.UUID

data class ImageConfiguration(
    val data: ByteArray,
    val ownerId: UUID
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? ImageConfiguration)?.let {
            data.contentEquals(it.data) && ownerId == it.ownerId
        } ?: false
    }

    override fun hashCode(): Int {
        return 31 * data.contentHashCode() + ownerId.hashCode()
    }
}
