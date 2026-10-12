package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ReviewDecision
import com.example.ui.screens.GooglePhotosScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReviewQueueScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SwipeStudioScreen
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CleanerViewModel

@Composable
fun CleanerApp(
    viewModel: CleanerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            // Only show bottom navigation on primary screens
            if (uiState.currentScreen == AppScreen.HOME ||
                uiState.currentScreen == AppScreen.GOOGLE_PHOTOS ||
                uiState.currentScreen == AppScreen.SETTINGS
            ) {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .border(1.dp, DarkBorder)
                ) {
                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonBright,
                            selectedTextColor = CrimsonBright,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.SWIPE_STUDIO,
                        onClick = { viewModel.startCleaningSession() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = "Swipe Studio"
                            )
                        },
                        label = { Text("Swipe") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonBright,
                            selectedTextColor = CrimsonBright,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_swipe")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.REVIEW_QUEUE,
                        onClick = { viewModel.navigateTo(AppScreen.REVIEW_QUEUE) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (uiState.markedCount > 0) {
                                        Badge(
                                            containerColor = CrimsonPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text(uiState.markedCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Review Queue"
                                )
                            }
                        },
                        label = { Text("Queue") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonBright,
                            selectedTextColor = CrimsonBright,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_queue")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.GOOGLE_PHOTOS,
                        onClick = { viewModel.navigateTo(AppScreen.GOOGLE_PHOTOS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Google Photos"
                            )
                        },
                        label = { Text("Photos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonBright,
                            selectedTextColor = CrimsonBright,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_cloud")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.SETTINGS,
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonBright,
                            selectedTextColor = CrimsonBright,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(
                        uiState = uiState,
                        onNavigate = { viewModel.navigateTo(it) },
                        onStartCleaning = { viewModel.startCleaningSession() },
                        onRefreshStats = { viewModel.loadStorageStats() },
                        onSelectFilter = { filter ->
                            viewModel.updateSettings(uiState.settings.copy(filterType = filter))
                        },
                        onCheckPermissions = { viewModel.checkPermissions() }
                    )

                    AppScreen.SWIPE_STUDIO -> SwipeStudioScreen(
                        uiState = uiState,
                        onBack = { viewModel.navigateTo(AppScreen.HOME) },
                        onNavigate = { viewModel.navigateTo(it) },
                        onKeep = { viewModel.decideCurrent(ReviewDecision.KEEP) },
                        onDelete = { viewModel.decideCurrent(ReviewDecision.DELETE) },
                        onSkip = { viewModel.decideCurrent(ReviewDecision.SKIP) },
                        onUndo = { viewModel.undo() },
                        onNext = { viewModel.nextCard() },
                        onPrev = { viewModel.previousCard() },
                        onReload = { viewModel.refreshMediaList() }
                    )

                    AppScreen.REVIEW_QUEUE -> ReviewQueueScreen(
                        uiState = uiState,
                        onBack = { viewModel.navigateTo(AppScreen.HOME) },
                        onNavigate = { viewModel.navigateTo(it) },
                        onRestoreItem = { entity -> viewModel.restoreItemFromQueue(entity) },
                        onClearAll = { viewModel.clearAllFromQueue() },
                        onRequestDelete = { viewModel.requestDeletionFlow() },
                        onDismissDialog = { viewModel.dismissDeletionDialog() },
                        onConfirmDeletion = { confirmed -> viewModel.executeDeletionConfirmed(confirmed) }
                    )

                    AppScreen.GOOGLE_PHOTOS -> GooglePhotosScreen(
                        uiState = uiState,
                        googlePhotosRepository = viewModel.googlePhotosRepository,
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        uiState = uiState,
                        onBack = { viewModel.navigateTo(AppScreen.HOME) },
                        onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                        onClearHistory = { viewModel.clearAllFromQueue() }
                    )
                }
            }
        }
    }
}
