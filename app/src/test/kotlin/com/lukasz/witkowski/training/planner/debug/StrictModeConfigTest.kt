package com.lukasz.witkowski.training.planner.debug

import android.os.StrictMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class StrictModeConfigTest {

    @Before
    fun setUp() {
        mockkStatic(StrictMode::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun allowDiskReads_executesBlockAndRestoresOldPolicy() {
        val oldPolicy = mockk<StrictMode.ThreadPolicy>()
        every { StrictMode.allowThreadDiskReads() } returns oldPolicy
        every { StrictMode.setThreadPolicy(any()) } returns Unit

        var executed = false
        val result = StrictModeConfig.allowDiskReads {
            executed = true
            "success"
        }

        assertEquals("success", result)
        assertEquals(true, executed)
        verify(exactly = 1) { StrictMode.allowThreadDiskReads() }
        verify(exactly = 1) { StrictMode.setThreadPolicy(oldPolicy) }
    }

    @Test
    fun allowDiskWrites_executesBlockAndRestoresOldPolicy() {
        val oldPolicy = mockk<StrictMode.ThreadPolicy>()
        every { StrictMode.allowThreadDiskWrites() } returns oldPolicy
        every { StrictMode.setThreadPolicy(any()) } returns Unit

        var executed = false
        val result = StrictModeConfig.allowDiskWrites {
            executed = true
            42
        }

        assertEquals(42, result)
        assertEquals(true, executed)
        verify(exactly = 1) { StrictMode.allowThreadDiskWrites() }
        verify(exactly = 1) { StrictMode.setThreadPolicy(oldPolicy) }
    }
}
