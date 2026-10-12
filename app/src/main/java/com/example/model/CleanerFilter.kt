package com.example.model

enum class MediaFilterType(val label: String) {
    ALL("All Media"),
    PHOTOS_ONLY("Photos Only"),
    VIDEOS_ONLY("Videos Only"),
    LARGE_FILES("Large Files (>25MB)"),
    SCREENSHOTS("Screenshots")
}

enum class SortOrder(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    SIZE_DESC("Largest First"),
    SIZE_ASC("Smallest First")
}

data class CleanerSettings(
    val filterType: MediaFilterType = MediaFilterType.ALL,
    val sortOrder: SortOrder = SortOrder.SIZE_DESC,
    val autoPlayVideo: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val showSizeBadges: Boolean = true
)
