package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.MediaItem
import com.example.model.MediaSource
import com.example.model.ReviewDecision
import com.example.model.StorageStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Storage Cleaner", appName)
    }

    @Test
    fun `formatBytes correctly formats units`() {
        assertEquals("500 B", MediaItem.formatBytes(500L))
        assertEquals("1.0 KB", MediaItem.formatBytes(1024L))
        assertEquals("1.5 MB", MediaItem.formatBytes((1.5 * 1024 * 1024).toLong()))
        assertEquals("10.0 GB", MediaItem.formatBytes(10L * 1024 * 1024 * 1024))
    }

    @Test
    fun `storageStats calculations are valid`() {
        val stats = StorageStats(
            totalDeviceBytes = 100L * 1024 * 1024 * 1024,
            freeDeviceBytes = 25L * 1024 * 1024 * 1024
        )
        assertEquals(75L * 1024 * 1024 * 1024, stats.usedDeviceBytes)
        assertEquals(0.75f, stats.deviceUsedPercent, 0.01f)
    }
}
