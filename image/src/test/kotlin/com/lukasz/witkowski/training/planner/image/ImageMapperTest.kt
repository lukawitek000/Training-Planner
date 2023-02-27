package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ImageMapperTest {

    @Test
    fun `convert image bitmap to byte array and back`() {
        val bitmap = givenTestBitmap()
        val image = ImageBitmap(bitmap)

        val byteArrayImage = image.toImageByteArray()
        val result = byteArrayImage.toBitmapImage()

        assertTrue(bitmap.sameAs(result.bitmap))
    }

    private fun givenTestBitmap(fileName: String = "test_icon.png"): Bitmap {
        val filePath = ClassLoader.getSystemResource(fileName).path
        return BitmapFactory.decodeFile(filePath)
    }
}
