package com.lukasz.witkowski.training.planner.image

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDao
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImageStorageTest {

    private lateinit var imageStorage: ImageStorage
    private lateinit var imgReferenceDatabase: ImageReferenceDatabase
    private lateinit var imageReferencedDao: ImageReferenceDao

    @Before
    fun setUp() {
        val applicationContext: Context = ApplicationProvider.getApplicationContext()
        val imgRepository = InternalStorageImageRepository(applicationContext, "testing_images")
        imgReferenceDatabase =
            Room.inMemoryDatabaseBuilder(applicationContext, ImageReferenceDatabase::class.java)
                .allowMainThreadQueries().build()
        imageReferencedDao = imgReferenceDatabase.imageReferenceDao()
        val imgRefRepository = DbImageReferenceRepository(imageReferencedDao)
        imageStorage = DataReferenceSeparatedImageStorage(imgRepository, imgRefRepository)
    }

    @After
    fun closeDb() {
        imgReferenceDatabase.close()
    }

    @Test
    fun `saves and read by reference the same byte array`() = runBlocking {
        val image = givenImageByteArray()

        val imageReference = imageStorage.saveImage(image)
        val readImage = imageStorage.readImage(imageReference.imageId)

        assertEquals(image.imageId, readImage.imageId)
        assertEquals(image.ownersIds, readImage.ownersIds)
        assertArrayEquals(image.data, readImage.data)
    }

    @Test
    fun `saves the image and read its reference`() = runBlocking {
        val image = givenImageByteArray()

        val imageReference = imageStorage.saveImage(image)
        val readReference = imageStorage.readImageReference(imageReference.imageId)

        assertEquals(imageReference, readReference)
    }

    @Test
    fun `read image reference for not existing image id`() = runBlocking {
        val dummyImageId = ImageId("dummyId")
        val readReference = imageStorage.readImageReference(dummyImageId)

        assertNull(readReference)
    }

    @Test
    fun `saving and deleting the image causes reading attempt to fail`() = runBlocking {
        val ownerId = "test_owner"
        val image = givenImageByteArray(ownersId = listOf(ownerId))

        val imageReference = imageStorage.saveImage(image)
        imageStorage.deleteImage(imageReference.imageId, ownerId)
        val exception = assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference.imageId)
        }
        println(exception.message)
    }

    @Test
    fun `saving image with two owners and deleting for one`() = runBlocking {
        val ownerId = "test_owner"
        val ownerToDelete = "test_delete_owner"
        val image = givenImageByteArray(ownersId = listOf(ownerId, ownerToDelete))

        val imageReference = imageStorage.saveImage(image)
        println("Image saved path ${imageReference.path}")
        imageStorage.deleteImage(imageReference.imageId, ownerToDelete)
        val readImage = imageStorage.readImage(imageReference.imageId)

        assertEquals(1, readImage.ownersIds.size)
        assertEquals(ownerId, readImage.ownersIds.first())
        assertEquals(image.imageId, readImage.imageId)
        assertArrayEquals(image.data, readImage.data)
    }

    @Test
    fun `saving image with two owners and deleting for each of them`() = runBlocking {
        val owner1 = "test_owner"
        val owner2 = "test_delete_owner"
        val image = givenImageByteArray(ownersId = listOf(owner1, owner2))

        val imageReference = imageStorage.saveImage(image)
        imageStorage.deleteImage(imageReference.imageId, owner1)
        imageStorage.deleteImage(imageReference.imageId, owner2)

        val exception = assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference.imageId)
        }
        println(exception.message)
    }

    @Test
    fun `delete image that does not exist throws exception`() = runBlocking {
        val ownerId = "wrong_ownerId"
        val exception = assertFailsWith<ImageNotFoundException> {
            imageStorage.deleteImage(ImageId("dummyId"), ownerId)
        }
        println(exception.message)
    }

    @Test
    fun `update saved image for the owner`() = runBlocking {
        val image = givenImageByteArray()
        val imageReference = imageStorage.saveImage(image)

        val newImage = givenImageByteArray(ImageId("updatedId"), data = TestData.updatedByteArray)
        val updatedImageReference = imageStorage.updateImage(image.imageId, newImage)
        val updatedImage = imageStorage.readImage(newImage.imageId)

        assertEquals(image.ownersIds, updatedImageReference.ownersIds)
        assertNotEquals(imageReference.imageId, updatedImageReference.imageId)
        assertByteArraysNotEqual(image.data, updatedImage.data)
    }

    @Test
    fun `generate new id if the new image has the same one`() = runBlocking {
        val imageId = ImageId("Same_id")
        val image = givenImageByteArray(imageId)
        imageStorage.saveImage(image)

        val newImage = givenImageByteArray(imageId, data = TestData.updatedByteArray)
        val result = imageStorage.updateImage(imageId, newImage)
        val updatedImage = imageStorage.readImageReference(result.imageId)

        assertNotEquals(imageId, updatedImage?.imageId)
    }

    @Test
    fun `update image for all owners`() = runBlocking {
        val owners = listOf("owner1", "owner2")
        val image = givenImageByteArray(ownersId = owners)

        imageStorage.saveImage(image)
        val newImage = givenImageByteArray(
            ImageId("UpdatedId"),
            data = TestData.updatedByteArray,
            ownersId = owners
        )
        val result = imageStorage.updateImage(image.imageId, newImage)
        val updatedImageReference = imageStorage.readImageReference(newImage.imageId)

        assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(image.imageId)
        }
        assertEquals(owners, result.ownersIds)
        assertEquals(updatedImageReference, result)
    }

    @Test
    fun `update image for only 2 of 4 owners`() = runBlocking {
        val owners = listOf("owner1", "owner2", "owner3", "owner4")
        val image = givenImageByteArray(ownersId = owners)

        imageStorage.saveImage(image)
        val newImage = givenImageByteArray(
            ImageId("UpdatedId"),
            data = TestData.updatedByteArray,
            ownersId = owners.take(2)
        )
        val result = imageStorage.updateImage(image.imageId, newImage)
        val updatedImageReference = imageStorage.readImageReference(newImage.imageId)

        val initialImageReference = imageStorage.readImageReference(image.imageId)
        assertEquals(owners.takeLast(2), initialImageReference?.ownersIds)
        assertEquals(image.imageId, initialImageReference?.imageId)

        assertEquals(newImage.imageId, result.imageId)
        assertEquals(owners.take(2), result.ownersIds)
        assertEquals(updatedImageReference, result)
    }

    @Test
    fun `update method saves image if it did not exist`() = runBlocking {
        val owners = listOf("owner1", "owner2")
        val image = givenImageByteArray(ownersId = owners)

        val imageReference = imageStorage.updateImage(ImageId("dummyId"), image)
        val result = imageStorage.readImage(imageReference.imageId)

        assertEquals(image.imageId, result.imageId)
        assertEquals(owners, result.ownersIds)
        assertArrayEquals(image.data, result.data)
    }

    private fun givenImageByteArray(
        imageId: ImageId = ImageId("testing_imageId"),
        ownersId: List<String> = listOf("owner1"),
        data: ByteArray = TestData.byteArray
    ): ImageByteArray {
        return ImageByteArray(imageId, ownersId, data)
    }

    private fun assertByteArraysNotEqual(
        expected: ByteArray,
        actual: ByteArray
    ) {
        var assertMessage = ""
        val sameSize = (expected.size == actual.size)
        if (sameSize) {
            for (i in expected.indices) {
                if (expected[i] != actual[i]) {
                    assertMessage =
                        "Different value at $i:\n expected:<${expected.size}> but was:<${actual.size}>"
                    break
                }
            }
        } else {
            assertMessage =
                "Different arrays size:\n expected:<${expected.size}> but was:<${actual.size}>"
        }
        assertTrue(assertMessage.isNotEmpty(), assertMessage)
    }
}
