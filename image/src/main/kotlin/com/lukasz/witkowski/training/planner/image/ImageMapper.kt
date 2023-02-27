package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.util.UUID
import com.lukasz.witkowski.training.planner.image.domain.Image as DomainImage
import com.lukasz.witkowski.training.planner.image.domain.ImageReference as DomainImageReference

private const val QUALITY_100 = 100

fun ImageBitmap.toImageByteArray(): ImageByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY_100, outputStream)
    val byteArray = outputStream.toByteArray()
    return ImageByteArray(byteArray)
}

fun Image.toBitmapImage(): ImageBitmap {
    return ImageByteArray(data).toBitmapImage()
}

fun ImageByteArray.toBitmapImage(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
    return ImageBitmap(bitmap)
}

fun ImageByteArray.toImageConfiguration(ownerId: UUID): ImageConfiguration {
    return ImageConfiguration(data, ownerId)
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
