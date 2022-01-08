package com.lukasz.witkowski.shared.utils

import android.content.Context
import android.content.Intent

fun <T> Context.stopSendingDataService(cls: Class<T>) = try {
    val intent = Intent(this, cls)
    stopService(intent)
    false
} catch (e: Exception) {
    true
}

fun <T> Context.startSendingDataService(cls: Class<T>) = try {
        val intent = Intent(this, cls)
        startService(intent)
        true
    } catch (e: Exception) {
        false
    }