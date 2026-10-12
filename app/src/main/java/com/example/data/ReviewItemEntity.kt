package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.MediaItem
import com.example.model.MediaSource
import com.example.model.ReviewDecision

@Entity(
    tableName = "review_items",
    indices = [Index(value = ["uri"], unique = true)]
)
data class ReviewItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaId: Long,
    val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String,
    val isVideo: Boolean,
    val decision: String,
    val decidedAt: Long = System.currentTimeMillis(),
    val durationMs: Long? = null
) {
    fun toMediaItem(): MediaItem {
        return MediaItem(
            id = mediaId,
            uri = uri,
            displayName = displayName,
            sizeBytes = sizeBytes,
            mimeType = mimeType,
            dateAdded = decidedAt / 1000,
            isVideo = isVideo,
            durationMs = durationMs,
            source = if (uri.startsWith("sample://")) MediaSource.SAMPLE else MediaSource.LOCAL
        )
    }

    companion object {
        fun fromMediaItem(item: MediaItem, decision: ReviewDecision): ReviewItemEntity {
            return ReviewItemEntity(
                mediaId = item.id,
                uri = item.uri,
                displayName = item.displayName,
                sizeBytes = item.sizeBytes,
                mimeType = item.mimeType,
                isVideo = item.isVideo,
                decision = decision.name,
                decidedAt = System.currentTimeMillis(),
                durationMs = item.durationMs
            )
        }
    }
}
