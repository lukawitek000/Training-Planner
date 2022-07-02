package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import com.lukasz.witkowski.training.planner.exercise.domain.ImageRepository
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ImageFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class InternalStorageImageRepository(
    private val context: Context,
    private val directoryName: String
) : ImageRepository {

    override suspend fun save(image: Image, fileName: String) = withContext(Dispatchers.IO) {
        var outputStream: FileOutputStream? = null
        try {
            val file = createFile(fileName)
            outputStream = FileOutputStream(file)
            outputStream.write(image.data)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Saving file $fileName into $directoryName directory has failed.")
        } finally {
            closeStream(outputStream)
        }
    }

    override suspend fun read(fileName: String): Image? = withContext(Dispatchers.IO) {
        var inputStream: FileInputStream? = null
        try {
            val file = createFile(fileName)
            inputStream = FileInputStream(file)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            ImageFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            Timber.d("Image file '$fileName' not found")
            null
        } finally {
            closeStream(inputStream)
        }
    }

    override suspend fun delete(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(directoryName, fileName)
            file.delete()
        } catch (e: Exception) {
            Timber.d("Image file '$fileName' not could not be deleted.")
            false
        }
    }

    override suspend fun update(image: Image?, fileName: String) = withContext(Dispatchers.IO) {
        try {
            delete(fileName)
            image?.let { save(image, fileName) } ?: Unit
        } catch (e: Exception) {
            throw Exception("Couldn't update the image. Deleted failed.")
        }
    }

    private fun createFile(fileName: String) =
        File(context.getDir(directoryName, Context.MODE_PRIVATE), fileName)

    private fun closeStream(closable: Closeable?) {
        try {
            closable?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
