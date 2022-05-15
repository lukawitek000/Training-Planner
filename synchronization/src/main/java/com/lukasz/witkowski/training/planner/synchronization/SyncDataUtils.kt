package com.lukasz.witkowski.shared.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

const val TRAINING_PATH = "/training"
const val STATISTICS_PATH = "/statistics"

val gson: Gson = GsonBuilder().create()


const val SYNC_SUCCESSFUL = 1
const val SYNC_FAILURE = 0

const val INTEGER_VALUE_BUFFER_SIZE = 4

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

fun Int.toByteArray(): ByteArray {
    val byteArray = ByteArray(INTEGER_VALUE_BUFFER_SIZE)
    for (i in 0..3) {
        byteArray[i] = (this shr (i * 8)).toByte()
    }
    return byteArray
}

fun ByteArray.toInt(): Int {
    return (this[3].toInt() shl 24) or (this[2].toInt() and 0xff shl 16) or (this[1].toInt() and 0xff shl 8) or (this[0].toInt() and 0xff)
}