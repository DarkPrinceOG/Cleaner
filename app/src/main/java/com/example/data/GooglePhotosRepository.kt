package com.example.data

import android.net.Uri
import com.example.model.MediaItem
import com.example.model.MediaSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class CloudAuthStatus {
    object Disconnected : CloudAuthStatus()
    data class Connected(val userEmail: String, val cloudUsedBytes: Long, val cloudTotalBytes: Long) : CloudAuthStatus()
    object ScopedPickerReady : CloudAuthStatus()
}

data class CloudCleanSession(
    val selectedCount: Int = 0,
    val selectedTotalBytes: Long = 0L,
    val items: List<MediaItem> = emptyList(),
    val notes: String = "Google Photos Picker enforces per-item user authorization. Cloud files cannot be deleted programmatically without individual user confirmation."
)

interface GooglePhotosRepository {
    val authStatus: Flow<CloudAuthStatus>
    val currentSession: Flow<CloudCleanSession>

    suspend fun connectCloudAccount(email: String = "user@gmail.com")
    suspend fun disconnectCloudAccount()
    suspend fun importPickedPhotos(uris: List<Uri>, displayNames: List<String>, sizes: List<Long>)
    fun clearCloudSession()
}

class GooglePhotosRepositoryImpl : GooglePhotosRepository {
    private val _authStatus = MutableStateFlow<CloudAuthStatus>(CloudAuthStatus.ScopedPickerReady)
    override val authStatus: Flow<CloudAuthStatus> = _authStatus.asStateFlow()

    private val _currentSession = MutableStateFlow(CloudCleanSession())
    override val currentSession: Flow<CloudCleanSession> = _currentSession.asStateFlow()

    override suspend fun connectCloudAccount(email: String) {
        _authStatus.value = CloudAuthStatus.Connected(
            userEmail = email,
            cloudUsedBytes = 11L * 1024 * 1024 * 1024 + 400L * 1024 * 1024,
            cloudTotalBytes = 15L * 1024 * 1024 * 1024
        )
    }

    override suspend fun disconnectCloudAccount() {
        _authStatus.value = CloudAuthStatus.ScopedPickerReady
        _currentSession.value = CloudCleanSession()
    }

    override suspend fun importPickedPhotos(
        uris: List<Uri>,
        displayNames: List<String>,
        sizes: List<Long>
    ) {
        val newItems = uris.mapIndexed { index, uri ->
            val name = displayNames.getOrNull(index) ?: "Google_Photo_${System.currentTimeMillis()}_$index.jpg"
            val size = sizes.getOrNull(index) ?: (6L * 1024 * 1024)
            MediaItem(
                id = 900000L + index,
                uri = uri.toString(),
                displayName = name,
                sizeBytes = size,
                mimeType = "image/jpeg",
                dateAdded = System.currentTimeMillis() / 1000,
                isVideo = false,
                bucketName = "Google Cloud Photos",
                source = MediaSource.GOOGLE_PHOTOS
            )
        }
        val allItems = _currentSession.value.items + newItems
        val totalBytes = allItems.sumOf { it.sizeBytes }
        _currentSession.value = CloudCleanSession(
            selectedCount = allItems.size,
            selectedTotalBytes = totalBytes,
            items = allItems
        )
    }

    override fun clearCloudSession() {
        _currentSession.value = CloudCleanSession()
    }
}
