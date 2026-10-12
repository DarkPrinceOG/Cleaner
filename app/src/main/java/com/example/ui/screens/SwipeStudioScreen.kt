package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import com.example.model.ReviewDecision
import com.example.ui.components.SwipeCard
import com.example.ui.theme.AmberSkip
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldKeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CleanerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeStudioScreen(
    uiState: CleanerUiState,
    onBack: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onKeep: () -> Unit,
    onDelete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onReload: () -> Unit
) {
    BackHandler { onBack() }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Swipe Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val total = uiState.mediaQueue.size
                        val currentNum = (uiState.currentIndex + 1).coerceAtMost(total)
                        Text(
                            text = if (total > 0) "File $currentNum of $total" else "No files loaded",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("action_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Queue shortcut counter pill
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, if (uiState.markedCount > 0) CrimsonPrimary else DarkBorder, RoundedCornerShape(12.dp))
                            .clickable { onNavigate(AppScreen.REVIEW_QUEUE) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (uiState.markedCount > 0) CrimsonBright else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.markedCount} (${MediaItem.formatBytes(uiState.markedTotalBytes)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.markedCount > 0) CrimsonBright else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Linear progress indicator
            LinearProgressIndicator(
                progress = { uiState.progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CrimsonPrimary,
                trackColor = DarkSurfaceElevated
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Card Display or Session Finished View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val currentMedia = uiState.currentMedia
                if (currentMedia != null && uiState.hasCardsRemaining) {
                    SwipeCard(
                        mediaItem = currentMedia,
                        autoPlayVideo = uiState.settings.autoPlayVideo,
                        onKeep = onKeep,
                        onDelete = onDelete,
                        onSkip = onSkip,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Finished Empty State
                    SessionCompletedCard(
                        reviewedCount = uiState.totalReviewedCount,
                        markedCount = uiState.markedCount,
                        markedBytes = uiState.markedTotalBytes,
                        onOpenQueue = { onNavigate(AppScreen.REVIEW_QUEUE) },
                        onReload = onReload
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Accessible On-Screen Controls Row
            SwipeControlsRow(
                canUndo = uiState.lastAction != null,
                canNavigatePrev = uiState.currentIndex > 0,
                canNavigateNext = uiState.currentIndex < uiState.mediaQueue.size - 1,
                onUndo = onUndo,
                onDelete = onDelete,
                onSkip = onSkip,
                onKeep = onKeep,
                onPrev = onPrev,
                onNext = onNext
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SwipeControlsRow(
    canUndo: Boolean,
    canNavigatePrev: Boolean,
    canNavigateNext: Boolean,
    onUndo: () -> Unit,
    onDelete: () -> Unit,
    onSkip: () -> Unit,
    onKeep: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev card
        IconButton(
            onClick = onPrev,
            enabled = canNavigatePrev,
            modifier = Modifier.testTag("action_prev")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Card",
                tint = if (canNavigatePrev) TextPrimary else TextMuted
            )
        }

        // Undo
        IconButton(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier.testTag("action_undo")
        ) {
            Icon(
                imageVector = Icons.Default.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) Color(0xFFFBBF24) else TextMuted
            )
        }

        // Delete (Left Action) - Crimson
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(CrimsonPrimary)
                .clickable(onClick = onDelete)
                .testTag("action_delete"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Mark for Deletion",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Skip (Up Action) - Amber
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AmberSkip.copy(alpha = 0.2f))
                .border(1.5.dp, AmberSkip, CircleShape)
                .clickable(onClick = onSkip)
                .testTag("action_skip"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Skip Item",
                tint = AmberSkip,
                modifier = Modifier.size(24.dp)
            )
        }

        // Keep (Right Action) - Emerald
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(EmeraldKeep)
                .clickable(onClick = onKeep)
                .testTag("action_keep"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Keep Item",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Next card
        IconButton(
            onClick = onNext,
            enabled = canNavigateNext,
            modifier = Modifier.testTag("action_next")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Card",
                tint = if (canNavigateNext) TextPrimary else TextMuted
            )
        }
    }
}

@Composable
private fun SessionCompletedCard(
    reviewedCount: Int,
    markedCount: Int,
    markedBytes: Long,
    onOpenQueue: () -> Unit,
    onReload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurfaceElevated)
            .border(1.5.dp, DarkBorder, RoundedCornerShape(24.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CrimsonPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CrimsonBright,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$markedCount items marked for cleanup",
                style = MaterialTheme.typography.titleMedium,
                color = CrimsonBright,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Estimated recovery: ${MediaItem.formatBytes(markedBytes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenQueue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_view_queue_completed")
            ) {
                Text(
                    text = "Review Marked Files (${markedCount})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onReload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBackground,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan More Media",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
