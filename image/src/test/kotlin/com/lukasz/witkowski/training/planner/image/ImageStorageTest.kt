package com.lukasz.witkowski.training.planner.image

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lukasz.witkowski.training.planner.image.infrastructure.Adler32ChecksumCalculator
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
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
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
    fun `read image reference for not existing image id returns null`() = runBlocking {
        val dummyImageId = ImageId("abc123".toUUID())
        val readReference = imageStorage.readImageReference(dummyImageId)

        assertNull(readReference)
    }

    @Test
    fun `read deleted image throws exception`() = runBlocking {
        val ownerId = "abc123".toUUID()
        val imageConfiguration = givenImageConfiguration(ownerId = ownerId)

        val imageReference = imageStorage.saveImage(imageConfiguration)
        imageStorage.deleteImage(imageReference.imageId, ownerId)
        assertFailsWith<ImageNotFoundException> {
            imageStorage.readImage(imageReference.imageId)
        }
        Unit
    }

    @Test
    fun `delete image for one owner`() = runBlocking {
        val ownerId = "abc123".toUUID()
        val ownerToDelete = "5463".toUUID()
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
    fun `read deleted image for all owners throws exception`() = runBlocking {
        val owner1 = "abc123".toUUID()
        val ownerToDelete = "5673".toUUID()
        val imageConfiguration1 = givenImageConfiguration(ownerId = owner1)
        val imageConfiguration2 = givenImageConfiguration(ownerId = ownerToDelete)

        val imageReference1 = imageStorage.saveImage(imageConfiguration1)
        val imageReference2 = imageStorage.saveImage(imageConfiguration2)
        imageStorage.deleteImage(imageReference1.imageId, owner1)
        imageStorage.deleteImage(imageReference2.imageId, ownerToDelete)

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
        val ownerId = "abc123".toUUID()
        assertFailsWith<ImageNotFoundException> {
            imageStorage.deleteImage(ImageId("12345".toUUID()), ownerId)
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
    @Suppress("LongMethod")
    fun `update image for all owners`() = runBlocking {
        val owners = listOf("abc123".toUUID(), "123abc".toUUID())
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

    @Test
    fun `update image for only 1 of 2 owners`() = runBlocking {
        val owners = listOf("abc123".toUUID(), "123abc".toUUID())
        val imageConfiguration1 = givenImageConfiguration(ownerId = owners[0])
        val imageConfiguration2 = givenImageConfiguration(ownerId = owners[1])

        val imageReference1 = imageStorage.saveImage(imageConfiguration1)
        imageStorage.saveImage(imageConfiguration2)

        val newImageConfiguration = givenImageConfiguration(ownerId = owners[0], data = TestData.updatedByteArray)
        val updateResult = imageStorage.updateImage(imageReference1.imageId, newImageConfiguration)
        val updatedImageReference = imageStorage.readImageReference(updateResult.imageId)
        val updatedImage = imageStorage.readImage(updatedImageReference!!.imageId)

        val initialImageReference = imageStorage.readImageReference(imageReference1.imageId)
        val initialImage = imageStorage.readImage(imageReference1.imageId)

        assertEquals(imageReference1.imageId, initialImageReference?.imageId)
        assertEquals(updatedImageReference, updateResult)
        assertArrayEquals(TestData.updatedByteArray, updatedImage.data)
        assertArrayEquals(TestData.byteArray, initialImage.data)
    }

    @Test
    fun `update method saves image if it did not exist`() = runBlocking {
        val imageConfiguration = givenImageConfiguration()
        val notExistingImageId = ImageId("abc123".toUUID())

        val imageReference = imageStorage.updateImage(notExistingImageId, imageConfiguration)
        val result = imageStorage.readImage(notExistingImageId)

        assertEquals(notExistingImageId, imageReference.imageId)
        assertEquals(notExistingImageId, result.imageId)
        assertArrayEquals(imageConfiguration.data, result.data)
    }

    private fun givenImageConfiguration(
        ownerId: UUID = "abc123".toUUID(),
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

    private fun String.toUUID() = UUID.fromString("$this-000-000-000-000")
}
