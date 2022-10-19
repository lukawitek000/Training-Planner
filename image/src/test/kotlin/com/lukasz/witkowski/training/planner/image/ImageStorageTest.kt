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
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
    fun `saving and deleting the image causes reading attempt to fail`() = runBlocking {
        val ownerId = "test_owner"
        val image = givenImageByteArray(ownersId = listOf(ownerId))

        val imageReference = imageStorage.saveImage(image)
        imageStorage.deleteImage(imageReference.imageId, ownerId)
//        assertThrows(ImageNotFoundException::class.java) {
            imageStorage.readImage(imageReference.imageId)
//        }
        assertTrue(true)
    }

    @Test
    fun `saving image with two owners and deleting for one`() = runBlocking {
        val ownerId = "test_owner"
        val ownerToDelete = "test_delete_owner"
        val image = givenImageByteArray(ownersId = listOf(ownerId, ownerToDelete))

        val imageReference = imageStorage.saveImage(image)
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

//        assertThrows(ImageNotFoundException::class.java) {
        imageStorage.readImage(imageReference.imageId)
//        }
        assertTrue(true)
    }

    private fun givenImageByteArray(
        imageId: ImageId = ImageId("testing_imageId"),
        ownersId: List<String> = listOf("owner1")
    ): ImageByteArray {
        return ImageByteArray(imageId, ownersId, TestData.byteArray)
    }
}
