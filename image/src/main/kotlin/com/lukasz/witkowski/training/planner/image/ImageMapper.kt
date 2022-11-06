package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
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

    fun toImageConfiguration(imageByteArray: ImageByteArray, ownerId: String): ImageConfiguration {
        return ImageConfiguration(imageByteArray.data, ownerId)
    }

    internal fun toImage(domainImage: DomainImage): Image {
        return Image(domainImage.imageId, domainImage.data)
    }

    internal fun toImageReference(domainImageReference: DomainImageReference): ImageReference {
        return ImageReference(domainImageReference.imageId, domainImageReference.path)
    }

    private const val QUALITY_100 = 100
}
