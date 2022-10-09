package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import com.lukasz.witkowski.training.planner.exercise.domain.ImageByteArray
import com.lukasz.witkowski.training.planner.exercise.domain.ImageReference
import com.lukasz.witkowski.training.planner.exercise.domain.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class InternalStorageImageRepository(
    context: Context,
    private val directoryName: String
) : ImageRepository {

    private val directoryPath = context.getDir(directoryName, Context.MODE_PRIVATE)

    override suspend fun save(image: ImageByteArray): ImageReference = withContext(Dispatchers.IO) {
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
        ImageReference(image.id, directoryPath.absolutePath)
    }

    override suspend fun delete(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(directoryPath, fileName)
            file.delete()
        } catch (e: Exception) {
            Timber.d("Image file '$fileName' not could not be deleted.")
            false
        }
    }

    override suspend fun update(image: ImageByteArray?, oldFileName: String) =
        withContext(Dispatchers.IO) {
            try {
                delete(oldFileName)
                image?.let { save(image) }
            } catch (e: Exception) {
                throw Exception("Couldn't update the image. Deleted failed.")
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
