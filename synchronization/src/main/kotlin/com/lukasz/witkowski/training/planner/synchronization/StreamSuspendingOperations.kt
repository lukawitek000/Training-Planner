package com.lukasz.witkowski.training.planner.synchronization

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

private const val INTEGER_VALUE_BUFFER_SIZE = 4

suspend fun OutputStream.closeSuspending() = withContext(Dispatchers.IO) {
    close()
}

suspend fun OutputStream.writeSuspending(byteArray: ByteArray) = withContext(Dispatchers.IO) {
    write(byteArray)
    flush()
}

suspend fun OutputStream.writeByteSuspending(number: Int) = withContext(Dispatchers.IO) {
    write(number)
}

suspend fun OutputStream.writeIntSuspending(number: Int) = withContext(Dispatchers.IO) {
    val byteArray = number.toByteArray()
    writeSuspending(byteArray)
}

suspend fun InputStream.closeSuspending() = withContext(Dispatchers.IO) {
    close()
}

suspend fun InputStream.readByteSuspending() = withContext(Dispatchers.IO) {
    read()
}

suspend fun InputStream.readByteSuspending(buffer: ByteArray): Int = withContext(Dispatchers.IO) {
    read(buffer)
}

suspend fun InputStream.readIntSuspending(): Int = withContext(Dispatchers.IO) {
    val buffer = ByteArray(INTEGER_VALUE_BUFFER_SIZE)
    read(buffer)
    buffer.toInt()
}

@Suppress("MagicNumber")
private fun Int.toByteArray(): ByteArray {
    val byteArray = ByteArray(INTEGER_VALUE_BUFFER_SIZE)
    for (i in 0..3) {
        byteArray[i] = (this shr (i * 8)).toByte()
    }
    return byteArray
}

@Suppress("MagicNumber")
private fun ByteArray.toInt() =
    ((this[3].toInt() shl 24) or (this[2].toInt() and 0xff shl 16)
            or (this[1].toInt() and 0xff shl 8) or (this[0].toInt() and 0xff))
