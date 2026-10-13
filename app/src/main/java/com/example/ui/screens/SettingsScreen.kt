package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanerSettings
import com.example.model.MediaFilterType
import com.example.model.SortOrder
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.model.SmartCategory
import com.example.viewmodel.CleanerUiState
import com.example.viewmodel.ResetScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: CleanerUiState,
    onBack: () -> Unit,
    onUpdateSettings: (CleanerSettings) -> Unit,
    onRequestReset: (ResetScope) -> Unit,
    onClearHistory: () -> Unit
) {
    BackHandler { onBack() }
    val settings = uiState.settings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cleaner Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("action_back_settings")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section: Media Filters
            item {
                SectionHeader(
                    icon = Icons.Default.FilterList,
                    title = "Media Filter"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        MediaFilterType.values().forEach { filter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onUpdateSettings(settings.copy(filterType = filter))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.filterType == filter,
                                    onClick = { onUpdateSettings(settings.copy(filterType = filter)) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CrimsonPrimary,
                                        unselectedColor = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = if (settings.filterType == filter) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Section: Sort Order
            item {
                SectionHeader(
                    icon = Icons.Default.Sort,
                    title = "Sort Order"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        SortOrder.values().forEach { sort ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onUpdateSettings(settings.copy(sortOrder = sort))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.sortOrder == sort,
                                    onClick = { onUpdateSettings(settings.copy(sortOrder = sort)) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CrimsonPrimary,
                                        unselectedColor = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sort.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = if (settings.sortOrder == sort) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Section: Review Preferences
            item {
                SectionHeader(
                    icon = Icons.Default.Tune,
                    title = "Review Preferences"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-play Videos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Play video previews automatically in Swipe Studio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = settings.autoPlayVideo,
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(autoPlayVideo = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CrimsonPrimary,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkBackground
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Haptic Drag Feedback",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Vibrate subtly when swipe threshold is reached",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = settings.hapticFeedbackEnabled,
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(hapticFeedbackEnabled = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CrimsonPrimary,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkBackground
                                )
                            )
                        }
                    }
                }
            }

            // Section: Review History Management
            item {
                SectionHeader(
                    icon = Icons.Default.DeleteSweep,
                    title = "Review History Management"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Resetting review history clears KEEP and SKIP decisions so photos reappear for review. Crucially, actual files are never deleted and pending deletion decisions remain safely preserved in the Review Queue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Reset This Folder
                        Button(
                            onClick = { onRequestReset(ResetScope.THIS_FOLDER) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceElevated,
                                contentColor = CrimsonBright
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.dp, CrimsonPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .testTag("btn_reset_current_folder")
                        ) {
                            Text(
                                text = "Reset ${uiState.selectedCategory.label} History",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Reset All
                        Button(
                            onClick = { onRequestReset(ResetScope.ALL) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceElevated,
                                contentColor = Color(0xFFFBBF24)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .testTag("btn_reset_all")
                        ) {
                            Text(
                                text = "Reset All Folders Review History",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CrimsonBright,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
