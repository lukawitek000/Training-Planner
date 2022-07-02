package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import com.lukasz.witkowski.training.planner.exercise.domain.ImageSaver
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ImageFactory
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class InternalStorageImageSaver(private val context: Context, private val directoryName: String) :
    ImageSaver {

    override fun save(image: Image, fileName: String) {
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

    override fun read(fileName: String): Image {
        var inputStream: FileInputStream? = null
        try {
            val file = createFile(fileName)
            inputStream = FileInputStream(file)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return ImageFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            throw FileNotFoundException("$fileName image has not been found in the internal storage in the $directoryName directory.")
        } finally {
            closeStream(inputStream)
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
