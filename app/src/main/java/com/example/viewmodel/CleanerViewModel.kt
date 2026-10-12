package com.example.viewmodel

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CleanerDatabase
import com.example.data.CloudAuthStatus
import com.example.data.CloudCleanSession
import com.example.data.GooglePhotosRepository
import com.example.data.GooglePhotosRepositoryImpl
import com.example.data.MediaStoreRepository
import com.example.data.PermissionState
import com.example.data.ReviewDao
import com.example.data.ReviewItemEntity
import com.example.model.CleanerSettings
import com.example.model.MediaFilterType
import com.example.model.MediaItem
import com.example.model.ReviewDecision
import com.example.model.SortOrder
import com.example.model.StorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    SWIPE_STUDIO,
    REVIEW_QUEUE,
    GOOGLE_PHOTOS,
    SETTINGS
}

data class CleanerUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val storageStats: StorageStats = StorageStats(),
    val mediaQueue: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val markedForDeletion: List<ReviewItemEntity> = emptyList(),
    val markedTotalBytes: Long = 0L,
    val markedCount: Int = 0,
    val totalReviewedCount: Int = 0,
    val permissionState: PermissionState = PermissionState.DENIED,
    val settings: CleanerSettings = CleanerSettings(),
    val isScanning: Boolean = false,
    val isDeleting: Boolean = false,
    val cloudAuthStatus: CloudAuthStatus = CloudAuthStatus.ScopedPickerReady,
    val cloudSession: CloudCleanSession = CloudCleanSession(),
    val showDeleteConfirmationDialog: Boolean = false,
    val pendingIntentSender: IntentSender? = null,
    val snackbarMessage: String? = null,
    val lastAction: Pair<MediaItem, ReviewDecision>? = null
) {
    val currentMedia: MediaItem?
        get() = mediaQueue.getOrNull(currentIndex)

    val hasCardsRemaining: Boolean
        get() = currentIndex < mediaQueue.size

    val progressRatio: Float
        get() = if (mediaQueue.isNotEmpty()) {
            (currentIndex.toFloat() / mediaQueue.size.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

class CleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CleanerDatabase.getInstance(application)
    private val reviewDao: ReviewDao = database.reviewDao()
    val mediaRepository = MediaStoreRepository(application, reviewDao)
    val googlePhotosRepository: GooglePhotosRepository = GooglePhotosRepositoryImpl()

    private val _uiState = MutableStateFlow(CleanerUiState())
    val uiState: StateFlow<CleanerUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20

    init {
        checkPermissions()
        loadStorageStats()
        observeDatabaseQueue()
        observeCloudState()
        refreshMediaList()
    }

    private fun observeDatabaseQueue() {
        viewModelScope.launch {
            combine(
                reviewDao.getMarkedForDeletion(),
                reviewDao.getMarkedDeleteTotalBytes(),
                reviewDao.getMarkedDeleteCount(),
                reviewDao.getTotalReviewedCount()
            ) { markedList, totalBytes, markedCount, totalReviewed ->
                Tuple4(markedList, totalBytes, markedCount, totalReviewed)
            }.collect { (markedList, totalBytes, markedCount, totalReviewed) ->
                _uiState.update { current ->
                    current.copy(
                        markedForDeletion = markedList,
                        markedTotalBytes = totalBytes,
                        markedCount = markedCount,
                        totalReviewedCount = totalReviewed,
                        storageStats = current.storageStats.copy(
                            markedDeleteBytes = totalBytes,
                            markedDeleteCount = markedCount,
                            reviewedCount = totalReviewed
                        )
                    )
                }
            }
        }
    }

    private fun observeCloudState() {
        viewModelScope.launch {
            googlePhotosRepository.authStatus.collect { status ->
                _uiState.update { it.copy(cloudAuthStatus = status) }
            }
        }
        viewModelScope.launch {
            googlePhotosRepository.currentSession.collect { session ->
                _uiState.update { it.copy(cloudSession = session) }
            }
        }
    }

    fun checkPermissions() {
        val perm = mediaRepository.checkPermissionStatus()
        _uiState.update { it.copy(permissionState = perm) }
    }

    fun loadStorageStats() {
        viewModelScope.launch {
            val stats = mediaRepository.getStorageStats()
            _uiState.update { it.copy(storageStats = stats) }
        }
    }

    fun refreshMediaList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            currentPage = 0
            val items = mediaRepository.loadMediaPage(
                page = currentPage,
                pageSize = pageSize,
                settings = _uiState.value.settings
            )
            _uiState.update {
                it.copy(
                    mediaQueue = items,
                    currentIndex = 0,
                    isScanning = false
                )
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun startCleaningSession() {
        if (_uiState.value.mediaQueue.isEmpty() || _uiState.value.currentIndex >= _uiState.value.mediaQueue.size) {
            refreshMediaList()
        }
        _uiState.update { it.copy(currentScreen = AppScreen.SWIPE_STUDIO) }
    }

    fun decideCurrent(decision: ReviewDecision) {
        val currentItem = _uiState.value.currentMedia ?: return

        viewModelScope.launch {
            // Save to Room persistent storage
            val entity = ReviewItemEntity.fromMediaItem(currentItem, decision)
            reviewDao.insertOrUpdate(entity)

            // Update ViewModel state
            _uiState.update { state ->
                val nextIndex = state.currentIndex + 1
                state.copy(
                    currentIndex = nextIndex,
                    lastAction = Pair(currentItem, decision)
                )
            }

            // If we are nearing end of batch, load next page
            if (_uiState.value.currentIndex >= _uiState.value.mediaQueue.size - 3) {
                loadNextBatch()
            }
        }
    }

    private fun loadNextBatch() {
        viewModelScope.launch {
            currentPage++
            val moreItems = mediaRepository.loadMediaPage(
                page = currentPage,
                pageSize = pageSize,
                settings = _uiState.value.settings
            )
            if (moreItems.isNotEmpty()) {
                _uiState.update { state ->
                    val combined = state.mediaQueue + moreItems.filter { item ->
                        state.mediaQueue.none { it.id == item.id }
                    }
                    state.copy(mediaQueue = combined)
                }
            }
        }
    }

    fun undo() {
        val last = _uiState.value.lastAction ?: return
        viewModelScope.launch {
            // Remove decision from Room
            reviewDao.deleteByUri(last.first.uri)

            _uiState.update { state ->
                val prevIndex = (state.currentIndex - 1).coerceAtLeast(0)
                state.copy(
                    currentIndex = prevIndex,
                    lastAction = null,
                    snackbarMessage = "Restored: ${last.first.displayName}"
                )
            }
        }
    }

    fun nextCard() {
        if (_uiState.value.currentIndex < _uiState.value.mediaQueue.size - 1) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }

    fun previousCard() {
        if (_uiState.value.currentIndex > 0) {
            _uiState.update { it.copy(currentIndex = it.currentIndex - 1) }
        }
    }

    fun restoreItemFromQueue(entity: ReviewItemEntity) {
        viewModelScope.launch {
            reviewDao.deleteById(entity.id)
            _uiState.update { it.copy(snackbarMessage = "Removed ${entity.displayName} from deletion queue") }
        }
    }

    fun clearAllFromQueue() {
        viewModelScope.launch {
            reviewDao.clearAll()
            _uiState.update { it.copy(snackbarMessage = "Review queue cleared") }
        }
    }

    fun requestDeletionFlow() {
        val marked = _uiState.value.markedForDeletion
        if (marked.isEmpty()) return

        val uris = marked.map { it.uri }
        val intentSender = mediaRepository.createPlatformDeleteRequest(uris)

        if (intentSender != null) {
            _uiState.update {
                it.copy(
                    pendingIntentSender = intentSender,
                    showDeleteConfirmationDialog = false
                )
            }
        } else {
            // For sample items or pre-API 30, open in-app confirmation modal
            _uiState.update {
                it.copy(
                    showDeleteConfirmationDialog = true,
                    pendingIntentSender = null
                )
            }
        }
    }

    fun dismissDeletionDialog() {
        _uiState.update {
            it.copy(
                showDeleteConfirmationDialog = false,
                pendingIntentSender = null
            )
        }
    }

    fun executeDeletionConfirmed(isPlatformSuccess: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, showDeleteConfirmationDialog = false) }
            val markedUris = _uiState.value.markedForDeletion.map { it.uri }
            val freedBytes = _uiState.value.markedTotalBytes

            mediaRepository.finalizeDeletion(markedUris, isPlatformSuccess)
            loadStorageStats()

            _uiState.update {
                it.copy(
                    isDeleting = false,
                    pendingIntentSender = null,
                    snackbarMessage = "Freed ${MediaItem.formatBytes(freedBytes)} from deletion queue!"
                )
            }
        }
    }

    fun updateSettings(newSettings: CleanerSettings) {
        _uiState.update { it.copy(settings = newSettings) }
        refreshMediaList()
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
