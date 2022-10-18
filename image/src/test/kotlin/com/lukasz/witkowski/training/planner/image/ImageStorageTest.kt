package com.lukasz.witkowski.training.planner.image

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ImageStorageTest {

    private lateinit var imageStorage: ImageStorage

    @Before
    fun setUp() {
        imageStorage = ImageStorageFactory.create()
    }


    @Test
    fun `saves and read by reference the same byte array`() {
        val imageId = ImageId("testing_imageId")
        val image = givenImageByteArray(imageId, "testing_owner_id")

        val imageReference = imageStorage.saveImage(image)
        val readImage = imageStorage.readImage(imageReference.imageId)

        assertEquals(image, readImage)
    }




    private fun givenImageByteArray(imageId: ImageId, ownerId: String = ""): ImageByteArray {
        return ImageByteArray(imageId, ownerId, TestData.byteArray)
    }
}
