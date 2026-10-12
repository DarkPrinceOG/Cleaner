package com.example.model

import java.util.Locale

enum class MediaSource {
    LOCAL,
    SAMPLE,
    GOOGLE_PHOTOS
}

data class MediaItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String,
    val dateAdded: Long,
    val isVideo: Boolean,
    val durationMs: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val bucketName: String? = null,
    val source: MediaSource = MediaSource.LOCAL
) {
    val formattedSize: String
        get() = formatBytes(sizeBytes)

    val formattedDuration: String?
        get() = durationMs?.let { formatDurationMs(it) }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val safeGroup = digitGroups.coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, safeGroup.toDouble())
            return if (safeGroup == 0) {
                String.format(Locale.US, "%.0f %s", value, units[safeGroup])
            } else {
                String.format(Locale.US, "%.1f %s", value, units[safeGroup])
            }
        }

        fun formatDurationMs(ms: Long): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }
}
