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
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.SmartCategory
import com.example.ui.components.SmartCategoryBar
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CleanerUiState
import com.example.viewmodel.ResetScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeStudioScreen(
    uiState: CleanerUiState,
    onBack: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onSelectCategory: (SmartCategory) -> Unit,
    onRequestReset: (ResetScope) -> Unit,
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
    var showMenu by remember { mutableStateOf(false) }

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
                            text = uiState.selectedCategory.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val total = uiState.mediaQueue.size
                        val currentNum = (uiState.currentIndex + 1).coerceAtMost(total)
                        Text(
                            text = if (total > 0) "File $currentNum of $total unreviewed" else "No unreviewed files",
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
                    // Reset History Menu Button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("action_reset_history_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset History",
                                tint = TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reset ${uiState.selectedCategory.label} History") },
                                onClick = {
                                    showMenu = false
                                    onRequestReset(ResetScope.THIS_FOLDER)
                                },
                                modifier = Modifier.testTag("menu_reset_folder")
                            )
                            DropdownMenuItem(
                                text = { Text("Reset All History") },
                                onClick = {
                                    showMenu = false
                                    onRequestReset(ResetScope.ALL)
                                },
                                modifier = Modifier.testTag("menu_reset_all")
                            )
                        }
                    }

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
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Smart Category Selector Bar
            SmartCategoryBar(
                selectedCategory = uiState.selectedCategory,
                categoryStats = uiState.categoryStats,
                onSelectCategory = onSelectCategory,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                        categoryLabel = uiState.selectedCategory.label,
                        reviewedCount = uiState.totalReviewedCount,
                        markedCount = uiState.markedCount,
                        markedBytes = uiState.markedTotalBytes,
                        onOpenQueue = { onNavigate(AppScreen.REVIEW_QUEUE) },
                        onResetFolder = { onRequestReset(ResetScope.THIS_FOLDER) },
                        onResetAll = { onRequestReset(ResetScope.ALL) },
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
    categoryLabel: String,
    reviewedCount: Int,
    markedCount: Int,
    markedBytes: Long,
    onOpenQueue: () -> Unit,
    onResetFolder: () -> Unit,
    onResetAll: () -> Unit,
    onReload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurfaceElevated)
            .border(1.5.dp, DarkBorder, RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CrimsonPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CrimsonBright,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "All $categoryLabel Reviewed!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$markedCount files marked for cleanup (${MediaItem.formatBytes(markedBytes)})",
                style = MaterialTheme.typography.bodyMedium,
                color = CrimsonBright,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (markedCount > 0) {
                Button(
                    onClick = onOpenQueue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_view_queue_completed")
                ) {
                    Text(
                        text = "Review Marked Files ($markedCount)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Reset This Folder Button
            Button(
                onClick = onResetFolder,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceElevated,
                    contentColor = CrimsonBright
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, CrimsonPrimary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .testTag("btn_reset_folder_completed")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset $categoryLabel History",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset All History Text Button
            TextButton(
                onClick = onResetAll,
                modifier = Modifier.testTag("btn_reset_all_completed")
            ) {
                Text(
                    text = "Reset All Folders History",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
