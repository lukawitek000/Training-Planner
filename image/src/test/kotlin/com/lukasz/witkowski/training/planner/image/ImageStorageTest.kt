package com.lukasz.witkowski.training.planner.image

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lukasz.witkowski.training.planner.image.infrastructure.Adler32ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDao
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDatabase
import junit.framework.Assert.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
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
        val checksumCalculator = Adler32ChecksumCalculator()
        imageStorage = DataReferenceSeparatedImageStorage(imgRepository, imgRefRepository, checksumCalculator)
    }

    @After
    fun closeDb() {
        imgReferenceDatabase.close()
    }

    @Test
    fun `saves and read by reference the same byte array`() = runBlocking {
        val imageConfiguration = givenImageConfiguration()

        val imageReference = imageStorage.saveImage(imageConfiguration)
        val readImage = imageStorage.readImage(imageReference.imageId)

        assertArrayEquals(imageConfiguration.data, readImage.data)
    }

    @Test
    fun `saves the image and read its reference`() = runBlocking {
        val image = givenImageConfiguration()

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
        val imageConfiguration = givenImageConfiguration(ownerId = ownerId)

        val imageReference = imageStorage.saveImage(imageConfiguration)
        imageStorage.deleteImage(imageReference.imageId, ownerId)
        assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference.imageId)
        }
        Unit
    }

    @Test
    fun `save the same image for two owners and deleting for one`() = runBlocking {
        val ownerId = "test_owner"
        val ownerToDelete = "test_delete_owner"
        val imageConfiguration1 = givenImageConfiguration(ownerId = ownerId)
        val imageConfiguration2 = givenImageConfiguration(ownerId = ownerToDelete)

        val imageReference1 = imageStorage.saveImage(imageConfiguration1)
        val imageReference2 = imageStorage.saveImage(imageConfiguration2)
        imageStorage.deleteImage(imageReference2.imageId, ownerToDelete)

        val readImage1 = imageStorage.readImage(imageReference1.imageId)
        assertArrayEquals(imageConfiguration1.data, readImage1.data)

        // The same image saved so it should be read from the storage without the exception
        val readImage2 = imageStorage.readImage(imageReference2.imageId)
        assertArrayEquals(imageConfiguration2.data, readImage2.data)
    }

    @Test
    fun `saving image with two owners and deleting for each of them`() = runBlocking {
        val owner1 = "test_owner"
        val owner2 = "test_delete_owner"
        val imageConfiguration1 = givenImageConfiguration(ownerId = owner1)
        val imageConfiguration2 = givenImageConfiguration(ownerId = owner2)

        val imageReference1 = imageStorage.saveImage(imageConfiguration1)
        val imageReference2 = imageStorage.saveImage(imageConfiguration2)
        imageStorage.deleteImage(imageReference1.imageId, owner1)
        imageStorage.deleteImage(imageReference2.imageId, owner2)

        assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference1.imageId)
        }
        assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference2.imageId)
        }
        Unit
    }

    @Test
    fun `delete image that does not exist throws exception`() = runBlocking {
        val ownerId = "wrong_ownerId"
        assertFailsWith<ImageNotFoundException> {
            imageStorage.deleteImage(ImageId("dummyId"), ownerId)
        }
        Unit
    }

    @Test
    fun `update saved image for the owner`() = runBlocking {
        val imageConfiguration = givenImageConfiguration()
        val imageReference = imageStorage.saveImage(imageConfiguration)

        val newImage = givenImageConfiguration(data = TestData.updatedByteArray)
        val updatedImageReference = imageStorage.updateImage(imageReference.imageId, newImage)
        val updatedImage = imageStorage.readImage(updatedImageReference.imageId)

        assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference.imageId)
        }
        assertNotEquals(imageReference.imageId, updatedImageReference.imageId)
        assertByteArraysNotEqual(imageConfiguration.data, updatedImage.data)
    }

    @Test
    fun `update image for all owners`() = runBlocking {
        val owners = listOf("owner1", "owner2")
        val imageConfiguration1 = givenImageConfiguration(ownerId = owners[0])
        val imageConfiguration2 = givenImageConfiguration(ownerId = owners[1])

        val imageReference1 = imageStorage.saveImage(imageConfiguration1)
        val imageReference2 = imageStorage.saveImage(imageConfiguration2)
        assertEquals(imageReference1, imageReference2)

        val updatedImageConfiguration1 = givenImageConfiguration(data = TestData.updatedByteArray, ownerId = owners[0])
        val updatedImageConfiguration2 = givenImageConfiguration(data = TestData.updatedByteArray, ownerId = owners[1])

        val result1 = imageStorage.updateImage(imageReference1.imageId, updatedImageConfiguration1)
        val result2 = imageStorage.updateImage(imageReference2.imageId, updatedImageConfiguration2)
        val updatedImageReference1 = imageStorage.readImageReference(result1.imageId)
        val updatedImageReference2 = imageStorage.readImageReference(result2.imageId)

        assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference1.imageId)
        }
        assertEquals(result1, updatedImageReference1)
        assertEquals(result2, updatedImageReference2)
        assertEquals(updatedImageReference1, updatedImageReference2)
    }

//    @Test
//    fun `update image for only 2 of 4 owners`() = runBlocking {
//        val owners = listOf("owner1", "owner2", "owner3", "owner4")
//        val image = givenImageByteArray(ownersId = owners)
//
//        imageStorage.saveImage(image)
//        val newImage = givenImageByteArray(
//            ImageId("UpdatedId"),
//            data = TestData.updatedByteArray,
//            ownersId = owners.take(2)
//        )
//        val result = imageStorage.updateImage(image.imageId, newImage)
//        val updatedImageReference = imageStorage.readImageReference(newImage.imageId)
//
//        val initialImageReference = imageStorage.readImageReference(image.imageId)
//        assertEquals(owners.takeLast(2), initialImageReference?.ownersIds)
//        assertEquals(image.imageId, initialImageReference?.imageId)
//
//        assertEquals(newImage.imageId, result.imageId)
//        assertEquals(owners.take(2), result.ownersIds)
//        assertEquals(updatedImageReference, result)
//    }
//
//    @Test
//    fun `update method saves image if it did not exist`() = runBlocking {
//        val owners = listOf("owner1", "owner2")
//        val image = givenImageByteArray(ownersId = owners)
//
//        val imageReference = imageStorage.updateImage(ImageId("dummyId"), image)
//        val result = imageStorage.readImage(imageReference.imageId)
//
//        assertEquals(image.imageId, result.imageId)
//        assertEquals(owners, result.ownersIds)
//        assertArrayEquals(image.data, result.data)
//    }
//
    private fun givenImageConfiguration(
        ownerId: String = "owner1",
        data: ByteArray = TestData.byteArray
    ): ImageConfiguration {
        return ImageConfiguration(data, ownerId)
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
