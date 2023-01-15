package com.lukasz.witkowski.training.planner.image

data class Image internal constructor(
    val imageId: ImageId,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? Image)?.let {
            data.contentEquals(it.data) && imageId == it.imageId
        } ?: false
    }

    override fun hashCode(): Int {
        return 31 * imageId.hashCode() + data.contentHashCode()
    }
}
