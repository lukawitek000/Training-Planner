package com.lukasz.witkowski.shared.utils

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

const val TRAINING_PATH = "/training"
const val STATISTICS_PATH = "/statistics"

val gson = GsonBuilder()
//    .registerTypeAdapter(Category::class.java, CategoryAdapter())
    .create()


const val SYNC_SUCCESSFUL = 1
const val SYNC_FAILURE = 0

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

suspend fun InputStream.readSuspending(buffer: ByteArray): Int = withContext(Dispatchers.IO) {
    read(buffer)
}

