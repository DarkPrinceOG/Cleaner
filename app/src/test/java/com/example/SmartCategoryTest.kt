package com.example

import com.example.model.MediaItem
import com.example.model.MediaSource
import com.example.model.SmartCategory
import com.example.model.SmartCategoryDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SmartCategoryTest {

    @Test
    fun `detects camera photos correctly`() {
        val cat1 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/jpeg",
            displayName = "IMG_20261012_001.jpg",
            relativePath = "DCIM/Camera/",
            bucketName = "Camera"
        )
        assertEquals(SmartCategory.CAMERA, cat1)

        val cat2 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/jpeg",
            displayName = "DSC_0042.jpg",
            relativePath = "DCIM/100ANDRO/",
            bucketName = "100ANDRO"
        )
        assertEquals(SmartCategory.CAMERA, cat2)
    }

    @Test
    fun `detects screenshots across different folder conventions`() {
        val cat1 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/png",
            displayName = "Screenshot_20261012-104904.png",
            relativePath = "Pictures/Screenshots/",
            bucketName = "Screenshots"
        )
        assertEquals(SmartCategory.SCREENSHOTS, cat1)

        val cat2 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/png",
            displayName = "my_app_screenshot.png",
            relativePath = "DCIM/Screenshots/",
            bucketName = "Screenshots"
        )
        assertEquals(SmartCategory.SCREENSHOTS, cat2)

        // Detected from filename even if folder is generic
        val cat3 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/png",
            displayName = "screenshot_statement.png",
            relativePath = "Pictures/",
            bucketName = "Pictures"
        )
        assertEquals(SmartCategory.SCREENSHOTS, cat3)
    }

    @Test
    fun `detects downloads folder`() {
        val cat1 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/jpeg",
            displayName = "invoice_report_2026.jpg",
            relativePath = "Download/",
            bucketName = "Download"
        )
        assertEquals(SmartCategory.DOWNLOADS, cat1)

        val cat2 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/png",
            displayName = "downloaded_asset.png",
            relativePath = "Download/Subfolder/",
            bucketName = "Downloads"
        )
        assertEquals(SmartCategory.DOWNLOADS, cat2)
    }

    @Test
    fun `detects whatsapp images`() {
        val cat1 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/jpeg",
            displayName = "IMG-20261012-WA0001.jpg",
            relativePath = "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/",
            bucketName = "WhatsApp Images"
        )
        assertEquals(SmartCategory.WHATSAPP, cat1)

        val cat2 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/jpeg",
            displayName = "family_photo.jpg",
            relativePath = "WhatsApp/Media/WhatsApp Images/",
            bucketName = "WhatsApp Images"
        )
        assertEquals(SmartCategory.WHATSAPP, cat2)
    }

    @Test
    fun `detects other images`() {
        val cat = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "image/png",
            displayName = "receipt_scan_01.png",
            relativePath = "Documents/",
            bucketName = "Documents"
        )
        assertEquals(SmartCategory.OTHER_IMAGES, cat)
    }

    @Test
    fun `detects videos regardless of folder`() {
        val catVideo1 = SmartCategoryDetector.detect(
            isVideo = true,
            mimeType = "video/mp4",
            displayName = "beach_clip.mp4",
            relativePath = "DCIM/Camera/",
            bucketName = "Camera"
        )
        assertEquals(SmartCategory.VIDEOS, catVideo1)

        val catVideo2 = SmartCategoryDetector.detect(
            isVideo = false,
            mimeType = "video/quicktime",
            displayName = "screen_recording.mov",
            relativePath = "Movies/",
            bucketName = "Movies"
        )
        assertEquals(SmartCategory.VIDEOS, catVideo2)
    }

    @Test
    fun `all categories are unique and identifiable`() {
        val ids = SmartCategory.entries.map { it.id }.toSet()
        assertEquals(7, ids.size)

        assertEquals(SmartCategory.CAMERA, SmartCategory.fromId("camera"))
        assertEquals(SmartCategory.SCREENSHOTS, SmartCategory.fromId("screenshots"))
        assertEquals(SmartCategory.DOWNLOADS, SmartCategory.fromId("downloads"))
        assertEquals(SmartCategory.WHATSAPP, SmartCategory.fromId("whatsapp"))
        assertEquals(SmartCategory.OTHER_IMAGES, SmartCategory.fromId("other_images"))
        assertEquals(SmartCategory.VIDEOS, SmartCategory.fromId("videos"))
        assertEquals(SmartCategory.ALL_MEDIA, SmartCategory.fromId("unknown"))
    }
}
