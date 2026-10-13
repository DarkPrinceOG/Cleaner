package com.example.model

import java.util.Locale

enum class SmartCategory(
    val id: String,
    val label: String
) {
    ALL_MEDIA("all_media", "All Media"),
    CAMERA("camera", "Camera"),
    SCREENSHOTS("screenshots", "Screenshots"),
    DOWNLOADS("downloads", "Downloads"),
    WHATSAPP("whatsapp", "WhatsApp Images"),
    OTHER_IMAGES("other_images", "Other Images"),
    VIDEOS("videos", "Videos");

    companion object {
        fun fromId(id: String): SmartCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALL_MEDIA
        }
    }
}

object SmartCategoryDetector {
    /**
     * Categorizes media using available metadata:
     * - isVideo / mimeType
     * - displayName
     * - relativePath
     * - bucketName
     */
    fun detect(
        isVideo: Boolean,
        mimeType: String,
        displayName: String,
        relativePath: String?,
        bucketName: String?
    ): SmartCategory {
        if (isVideo || mimeType.startsWith("video/", ignoreCase = true)) {
            return SmartCategory.VIDEOS
        }

        val name = displayName.lowercase(Locale.ROOT)
        val rel = relativePath?.lowercase(Locale.ROOT) ?: ""
        val bucket = bucketName?.lowercase(Locale.ROOT) ?: ""

        // 1. Screenshots
        if (name.contains("screenshot") ||
            bucket.contains("screenshot") ||
            rel.contains("screenshot") ||
            rel.startsWith("pictures/screenshots") ||
            rel.startsWith("dcim/screenshots")
        ) {
            return SmartCategory.SCREENSHOTS
        }

        // 2. WhatsApp Images
        if (bucket.contains("whatsapp") ||
            rel.contains("whatsapp") ||
            (name.startsWith("img-") && name.contains("-wa")) ||
            rel.contains("com.whatsapp")
        ) {
            return SmartCategory.WHATSAPP
        }

        // 3. Downloads
        if (bucket.contains("download") ||
            rel.startsWith("download") ||
            rel.contains("/download") ||
            bucket.contains("downloads")
        ) {
            return SmartCategory.DOWNLOADS
        }

        // 4. Camera
        if (bucket == "camera" ||
            bucket == "100andro" ||
            bucket == "dcim" ||
            rel.startsWith("dcim/camera") ||
            rel.startsWith("dcim/100andro") ||
            (rel.startsWith("dcim") && !rel.contains("screenshot"))
        ) {
            return SmartCategory.CAMERA
        }

        // 5. Other Images
        return SmartCategory.OTHER_IMAGES
    }
}
