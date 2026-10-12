package com.example.model

enum class ReviewDecision {
    KEEP,
    DELETE,
    SKIP
}

data class StorageStats(
    val totalDeviceBytes: Long = 128L * 1024 * 1024 * 1024,
    val freeDeviceBytes: Long = 42L * 1024 * 1024 * 1024,
    val photosBytes: Long = 18L * 1024 * 1024 * 1024,
    val photosCount: Int = 1240,
    val videosBytes: Long = 24L * 1024 * 1024 * 1024,
    val videosCount: Int = 158,
    val totalDiscovered: Int = 0,
    val reviewedCount: Int = 0,
    val markedDeleteCount: Int = 0,
    val markedDeleteBytes: Long = 0L,
    val cloudTotalBytes: Long = 15L * 1024 * 1024 * 1024,
    val cloudUsedBytes: Long = 11L * 1024 * 1024 * 1024
) {
    val usedDeviceBytes: Long
        get() = (totalDeviceBytes - freeDeviceBytes).coerceAtLeast(0)

    val deviceUsedPercent: Float
        get() = if (totalDeviceBytes > 0) (usedDeviceBytes.toFloat() / totalDeviceBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val cloudUsedPercent: Float
        get() = if (cloudTotalBytes > 0) (cloudUsedBytes.toFloat() / cloudTotalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val reviewProgressPercent: Float
        get() = if (totalDiscovered > 0) (reviewedCount.toFloat() / totalDiscovered.toFloat()).coerceIn(0f, 1f) else 0f
}
