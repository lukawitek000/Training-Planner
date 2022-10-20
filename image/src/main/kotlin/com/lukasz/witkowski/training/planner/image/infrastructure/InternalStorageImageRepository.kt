package com.lukasz.witkowski.training.planner.image.infrastructure

import android.content.Context
import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal class InternalStorageImageRepository(
    private val context: Context,
    private val directoryName: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageRepository {

    private val directoryPath = context.getDir(directoryName, Context.MODE_PRIVATE)

    override suspend fun save(image: ImageByteArray): ImageReference = withContext(ioDispatcher) {
        var outputStream: FileOutputStream? = null
        val fileName = image.imageName
        val file = createFile(fileName)
        try {
            outputStream = FileOutputStream(file)
            outputStream.write(image.data)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Saving file $fileName into $directoryName directory has failed.")
        } finally {
            closeStream(outputStream)
        }
        ImageReference(image.imageId, image.ownersIds, file.absolutePath)
    }

    override suspend fun read(imageReference: ImageReference): ImageByteArray? =
        withContext(ioDispatcher) {
            val filePath = imageReference.path
            try {
                val file = File(filePath)
                if (file.exists()) {
                    val byteArray = file.readBytes()
                    ImageByteArray(imageReference.imageId, imageReference.ownersIds, byteArray)
                } else {
                    Timber.w("Image does not exist under the path ${imageReference.path}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    override suspend fun delete(imageReference: ImageReference): Boolean =
        withContext(ioDispatcher) {
            val imagePath = imageReference.path
            try {
                val file = File(imagePath)
                file.delete()
            } catch (e: Exception) {
                Timber.w("Image file '$imagePath' not could not be deleted.")
                false
            }
        }

    override suspend fun update(
        image: ImageByteArray,
        oldImageReference: ImageReference
    ): ImageReference = withContext(ioDispatcher) {
        try {
            delete(oldImageReference)
            save(image)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Image update failed ${e.message}")
        }
    }

    private fun createFile(fileName: String) = File(directoryPath, fileName)

    private fun closeStream(closable: Closeable?) {
        try {
            closable?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
