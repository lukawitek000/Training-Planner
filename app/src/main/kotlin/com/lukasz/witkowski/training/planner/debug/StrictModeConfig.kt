package com.lukasz.witkowski.training.planner.debug

import android.os.Build
import android.os.StrictMode
import timber.log.Timber

/**
 * Diagnostic configuration for Android's [StrictMode].
 *
 * Catches main-thread disk/network IO, memory leaks (activities, SQLite, unclosed closeables),
 * and security issues (file URI exposure, cleartext traffic) early in debug sessions.
 */
object StrictModeConfig {

    fun configure() {
        Timber.d("Configuring StrictMode policies for debug build")

        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .detectCustomSlowCalls()
            .detectResourceMismatches()
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    detectUnbufferedIo()
                }
            }
            .penaltyLog()
            .penaltyFlashScreen()

        StrictMode.setThreadPolicy(threadPolicyBuilder.build())

        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedClosableObjects()
            .detectLeakedSqlLiteObjects()
            .detectLeakedRegistrationObjects()
            .detectFileUriExposure()
            .detectCleartextNetwork()
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    detectContentUriWithoutPermission()
                }
            }
            .penaltyLog()

        StrictMode.setVmPolicy(vmPolicyBuilder.build())
    }

    /**
     * Temporarily relaxes disk-read detection for [block], restoring previous policy upon return.
     * Useful when third-party libraries perform unavoidable synchronous disk reads on initialization.
     */
    inline fun <T> allowDiskReads(block: () -> T): T {
        val oldPolicy = StrictMode.allowThreadDiskReads()
        return try {
            block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    /**
     * Temporarily relaxes disk-write detection for [block], restoring previous policy upon return.
     */
    inline fun <T> allowDiskWrites(block: () -> T): T {
        val oldPolicy = StrictMode.allowThreadDiskWrites()
        return try {
            block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
}
