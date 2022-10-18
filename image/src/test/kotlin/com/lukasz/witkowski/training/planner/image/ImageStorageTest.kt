package com.lukasz.witkowski.training.planner.image

import androidx.test.core.app.ApplicationProvider
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageStorageTest {

    private lateinit var imageStorage: ImageStorage

    @Before
    fun setUp() {
        imageStorage = ImageStorageFactory.create(ApplicationProvider.getApplicationContext(), "testing_images")
    }


    @Test
    fun `saves and read by reference the same byte array`() = runBlocking {
        val imageId = ImageId("testing_imageId")
        val image = givenImageByteArray(imageId, "testing_owner_id")

        val imageReference = imageStorage.saveImage(image)
        println("Saved image path ${imageReference.path}")
        val readImage = imageStorage.readImage(imageReference.imageId)

        assertEquals(image, readImage)
    }


    private fun givenImageByteArray(imageId: ImageId, ownerId: String = ""): ImageByteArray {
        return ImageByteArray(imageId, ownerId, TestData.byteArray)
    }
}
