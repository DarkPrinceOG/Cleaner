package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PermissionState
import com.example.model.MediaFilterType
import com.example.model.MediaItem
import com.example.ui.components.StorageGauge
import com.example.ui.theme.CardGradientStart
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldKeep
import com.example.ui.theme.SkyPhotos
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CleanerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: CleanerUiState,
    onNavigate: (AppScreen) -> Unit,
    onStartCleaning: () -> Unit,
    onRefreshStats: () -> Unit,
    onSelectFilter: (MediaFilterType) -> Unit,
    onCheckPermissions: () -> Unit
) {
    // Permission launcher for media permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onCheckPermissions()
        onRefreshStats()
    }

    ScaffoldContainer(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CrimsonPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Storage Cleaner",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Native Media Optimization",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefreshStats,
                        modifier = Modifier.testTag("action_refresh_stats")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { onNavigate(AppScreen.SETTINGS) },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission status banner if not granted
            if (uiState.permissionState == PermissionState.DENIED) {
                item {
                    PermissionNoticeCard(
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.READ_MEDIA_IMAGES,
                                        android.Manifest.permission.READ_MEDIA_VIDEO,
                                        android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                                    )
                                )
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.READ_MEDIA_IMAGES,
                                        android.Manifest.permission.READ_MEDIA_VIDEO
                                    )
                                )
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                )
                            }
                        }
                    )
                }
            }

            // Storage Gauge Card
            item {
                StorageGauge(stats = uiState.storageStats)
            }

            // Start Cleaning Primary CTA
            item {
                Button(
                    onClick = onStartCleaning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("btn_start_cleaning"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Start Cleaning Session",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Quick Filter Row
            item {
                Column {
                    Text(
                        text = "CLEANING TARGET",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill(
                            label = "All Media",
                            selected = uiState.settings.filterType == MediaFilterType.ALL,
                            onClick = { onSelectFilter(MediaFilterType.ALL) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPill(
                            label = "Photos",
                            selected = uiState.settings.filterType == MediaFilterType.PHOTOS_ONLY,
                            onClick = { onSelectFilter(MediaFilterType.PHOTOS_ONLY) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPill(
                            label = "Videos",
                            selected = uiState.settings.filterType == MediaFilterType.VIDEOS_ONLY,
                            onClick = { onSelectFilter(MediaFilterType.VIDEOS_ONLY) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPill(
                            label = ">25MB",
                            selected = uiState.settings.filterType == MediaFilterType.LARGE_FILES,
                            onClick = { onSelectFilter(MediaFilterType.LARGE_FILES) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Review Progress Banner
            item {
                ReviewProgressCard(
                    reviewedCount = uiState.totalReviewedCount,
                    totalDiscovered = uiState.storageStats.totalDiscovered,
                    markedDeleteCount = uiState.markedCount,
                    markedDeleteBytes = uiState.markedTotalBytes,
                    onOpenQueue = { onNavigate(AppScreen.REVIEW_QUEUE) }
                )
            }

            // Media Source Breakdown Cards
            item {
                Text(
                    text = "STORAGE SOURCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                SourceCard(
                    icon = Icons.Default.Folder,
                    iconColor = CrimsonBright,
                    title = "Local Device Media",
                    subtitle = "${uiState.storageStats.photosCount} photos • ${uiState.storageStats.videosCount} videos",
                    tag = if (uiState.permissionState == PermissionState.FULL_ACCESS) "Active MediaStore" else "Sample/Limited",
                    tagColor = if (uiState.permissionState == PermissionState.FULL_ACCESS) EmeraldKeep else CoralAccent,
                    extraText = MediaItem.formatBytes(uiState.storageStats.photosBytes + uiState.storageStats.videosBytes),
                    onClick = onStartCleaning
                )
            }

            item {
                SourceCard(
                    icon = Icons.Default.Cloud,
                    iconColor = SkyPhotos,
                    title = "Google Photos",
                    subtitle = "Scoped Cloud Picker integration & photo review",
                    tag = "Cloud Integration",
                    tagColor = SkyPhotos,
                    extraText = "15 GB Quota",
                    onClick = { onNavigate(AppScreen.GOOGLE_PHOTOS) }
                )
            }

            item {
                SourceCard(
                    icon = Icons.Default.Delete,
                    iconColor = CrimsonPrimary,
                    title = "Deletion Review Queue",
                    subtitle = "${uiState.markedCount} files marked for final cleanup",
                    tag = if (uiState.markedCount > 0) "Ready to Clean" else "Empty",
                    tagColor = if (uiState.markedCount > 0) CrimsonBright else TextMuted,
                    extraText = MediaItem.formatBytes(uiState.markedTotalBytes),
                    onClick = { onNavigate(AppScreen.REVIEW_QUEUE) }
                )
            }
        }
    }
}

@Composable
private fun ScaffoldContainer(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = topBar,
        containerColor = DarkBackground,
        content = content
    )
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) CrimsonPrimary else DarkSurfaceElevated)
            .border(
                1.dp,
                if (selected) CrimsonBright else DarkBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ReviewProgressCard(
    reviewedCount: Int,
    totalDiscovered: Int,
    markedDeleteCount: Int,
    markedDeleteBytes: Long,
    onOpenQueue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenQueue)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Review Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$reviewedCount items reviewed • $markedDeleteCount marked for deletion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (markedDeleteBytes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${MediaItem.formatBytes(markedDeleteBytes)} estimated space savings",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonBright,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CrimsonGlow)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Queue",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonBright,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = CrimsonBright,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    tag: String,
    tagColor: Color,
    extraText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tagColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = tagColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = extraText,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionNoticeCard(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF23181C))
            .border(1.dp, CrimsonPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CrimsonBright,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Grant Media Access",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Enable permission to scan real photos and videos on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

