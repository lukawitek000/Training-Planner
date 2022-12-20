package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.util.UUID
import com.lukasz.witkowski.training.planner.image.domain.Image as DomainImage
import com.lukasz.witkowski.training.planner.image.domain.ImageReference as DomainImageReference

object ImageMapper {

    fun toImageByteArray(image: ImageBitmap): ImageByteArray {
        val outputStream = ByteArrayOutputStream()
        image.bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY_100, outputStream)
        val byteArray = outputStream.toByteArray()
        return ImageByteArray(byteArray)
    }

    fun toBitmapImage(image: Image): ImageBitmap {
        return toBitmapImage(ImageByteArray(image.data))
    }

    fun toBitmapImage(imageByteArray: ImageByteArray): ImageBitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray.data, 0, imageByteArray.data.size)
        return ImageBitmap(bitmap)
    }

    fun toImageConfiguration(imageByteArray: ImageByteArray, ownerId: UUID): ImageConfiguration {
        return ImageConfiguration(imageByteArray.data, ownerId)
    }

    private const val QUALITY_100 = 100
}

internal fun DomainImage.toImage(): Image {
    return Image(imageId, data)
}

internal fun DomainImageReference.toImageReference(): ImageReference {
    return ImageReference(imageId, path)
}

internal fun ImageConfiguration.toImageByteArray(): ImageByteArray {
    return ImageByteArray(data)
}
