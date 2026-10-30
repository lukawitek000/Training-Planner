package com.lukasz.witkowski.training.planner.image.infrastructure

import android.content.Context
import com.lukasz.witkowski.training.planner.image.ImageSaveFailedException
import com.lukasz.witkowski.training.planner.image.domain.Image
import com.lukasz.witkowski.training.planner.image.domain.ImageReference
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class InternalStorageImageRepository(
    private val context: Context,
    private val directoryName: String,
    private val ioDispatcher: CoroutineDispatcher
) : ImageRepository {
    private val directoryPath = context.getDir(directoryName, Context.MODE_PRIVATE)

    override suspend fun save(image: Image): ImageReference = withContext(ioDispatcher) {
        var outputStream: FileOutputStream? = null
        val fileName = image.imageName
        val file = createFile(fileName)
        try {
            outputStream = FileOutputStream(file)
            outputStream.write(image.data)
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.w("Saving file $fileName into $directoryName directory has failed.")
            throw ImageSaveFailedException(image.imageId)
        } finally {
            closeStream(outputStream)
        }
        ImageReference(image.imageId, image.ownersIds, file.absolutePath, image.checksum)
    }

    override suspend fun read(imageReference: ImageReference): Image? = withContext(ioDispatcher) {
        val filePath = imageReference.path
        if (filePath.startsWith("file:///android_asset/")) {
            val assetPath = filePath.removePrefix("file:///android_asset/")
            return@withContext try {
                val byteArray = context.assets.open(assetPath).use { it.readBytes() }
                Image(
                    imageReference.imageId,
                    imageReference.ownersIds,
                    byteArray,
                    imageReference.checksum
                )
            } catch (e: Exception) {
                Timber.w("Failed to read asset under path $filePath: ${e.message}")
                null
            }
        }
        try {
            val file = File(filePath)
            if (file.exists()) {
                val byteArray = file.readBytes()
                Image(
                    imageReference.imageId,
                    imageReference.ownersIds,
                    byteArray,
                    imageReference.checksum
                )
            } else {
                Timber.w("Image does not exist under the path ${imageReference.path}")
                null
            }
        } catch (e: SecurityException) {
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
            } catch (e: SecurityException) {
                Timber.w("Image delete was denied ${e.message}")
                false
            }
        }

    override suspend fun update(image: Image, oldImageReference: ImageReference): ImageReference =
        withContext(ioDispatcher) {
            delete(oldImageReference)
            save(image)
        }

    private fun createFile(fileName: String) = File(directoryPath, fileName)

    private fun closeStream(closable: Closeable?) {
        try {
            closable?.close()
        } catch (e: IOException) {
            Timber.w("Closing stream failed ${e.message}")
        }
    }
}
