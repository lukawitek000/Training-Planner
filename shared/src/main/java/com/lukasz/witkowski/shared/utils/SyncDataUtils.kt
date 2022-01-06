package com.lukasz.witkowski.shared.utils

import com.google.gson.GsonBuilder
import com.lukasz.witkowski.shared.models.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

const val TRAINING_PATH = "/training"
const val TRAINING_KEY = "training"

val gson = GsonBuilder()
    .registerTypeAdapter(Category::class.java, CategoryAdapter())
    .create()

suspend fun OutputStream.closeSuspending() = withContext(Dispatchers.IO) {
    close()
}

suspend fun OutputStream.writeSuspending(byteArray: ByteArray) = withContext(Dispatchers.IO) {
    write(byteArray)
    flush()
}
suspend fun OutputStream.writeIntSuspending(number: Int) = withContext(Dispatchers.IO) {
    write(number)
}

suspend fun InputStream.closeSuspending() = withContext(Dispatchers.IO) {
    close()
}

suspend fun InputStream.readSuspending() = withContext(Dispatchers.IO) {
    read()
}

