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
        val imageId = ImageId("testing_imageId")
        val image = givenImageByteArray(imageId)

        val imageReference = imageStorage.saveImage(image)
        val readImage = imageStorage.readImage(imageReference.imageId)

        assertEquals(image.imageId, readImage.imageId)
        assertEquals(image.ownersIds, readImage.ownersIds)
        assertArrayEquals(image.data, readImage.data)
    }


    private fun givenImageByteArray(
        imageId: ImageId,
        ownersId: List<String> = listOf("owner1")
    ): ImageByteArray {
        return ImageByteArray(imageId, ownersId, TestData.byteArray)
    }
}
