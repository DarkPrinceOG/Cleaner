package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MediaItem
import com.example.model.MediaSource
import com.example.ui.theme.AmberSkip
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldKeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeCard(
    mediaItem: MediaItem,
    autoPlayVideo: Boolean,
    onKeep: () -> Unit,
    onDelete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val swipeThreshold = 260f
    val skipThreshold = -260f

    val rotationZ = (offsetX.value / 25f).coerceIn(-18f, 18f)

    // Visual drag indicators
    val isDraggingRight = offsetX.value > 60f
    val isDraggingLeft = offsetX.value < -60f
    val isDraggingUp = offsetY.value < -60f && !isDraggingRight && !isDraggingLeft

    Box(
        modifier = modifier
            .testTag("swipe_media_card")
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .rotate(rotationZ)
            .pointerInput(mediaItem.id) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            when {
                                offsetX.value > swipeThreshold -> {
                                    // Swipe Right -> KEEP
                                    offsetX.animateTo(1000f, spring())
                                    onKeep()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                                offsetX.value < -swipeThreshold -> {
                                    // Swipe Left -> DELETE
                                    offsetX.animateTo(-1000f, spring())
                                    onDelete()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                                offsetY.value < skipThreshold -> {
                                    // Swipe Up -> SKIP
                                    offsetY.animateTo(-1000f, spring())
                                    onSkip()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                                else -> {
                                    // Return to center
                                    launch { offsetX.animateTo(0f, spring()) }
                                    launch { offsetY.animateTo(0f, spring()) }
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .border(1.5.dp, DarkBorder, RoundedCornerShape(24.dp))
    ) {
        // Content Media (Photo or Video)
        if (mediaItem.isVideo && !mediaItem.uri.startsWith("sample://")) {
            VideoPlayerView(
                uriString = mediaItem.uri,
                autoPlay = autoPlayVideo,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Photo or sample preview graphic
            val context = LocalContext.current
            val isSample = mediaItem.uri.startsWith("sample://")

            if (isSample) {
                // Dynamic decorative canvas preview for sample media
                SampleMediaGraphic(item = mediaItem)
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaItem.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = mediaItem.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Gradient overlay for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Top Badges (Media type & Source tag)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (mediaItem.isVideo) Icons.Default.Movie else Icons.Default.Photo,
                        contentDescription = null,
                        tint = if (mediaItem.isVideo) CrimsonBright else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (mediaItem.isVideo) "VIDEO" else "PHOTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (mediaItem.formattedDuration != null) {
                        Text(
                            text = " • ${mediaItem.formattedDuration}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Size Badge in Crimson glow
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrimsonPrimary.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = mediaItem.formattedSize,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Dynamic Drag Action Overlays
        if (isDraggingRight) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EmeraldKeep.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EmeraldKeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Keep",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "KEEP",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmeraldKeep,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        if (isDraggingLeft) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CrimsonPrimary.copy(alpha = 0.32f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CrimsonPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "MARK DELETE",
                        style = MaterialTheme.typography.titleLarge,
                        color = CrimsonBright,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        if (isDraggingUp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AmberSkip.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AmberSkip),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Skip",
                            tint = Color.Black,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "SKIP",
                        style = MaterialTheme.typography.titleLarge,
                        color = AmberSkip,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Bottom Metadata Details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = mediaItem.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (mediaItem.width != null && mediaItem.height != null) {
                    Text(
                        text = "${mediaItem.width}×${mediaItem.height}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(text = " • ", color = TextSecondary)
                }
                Text(
                    text = mediaItem.bucketName ?: "Gallery",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (mediaItem.source == MediaSource.SAMPLE) {
                    Text(text = " • ", color = TextSecondary)
                    Text(
                        text = "Sample Item",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleMediaGraphic(item: MediaItem) {
    val bgGradient = if (item.isVideo) {
        Brush.linearGradient(
            listOf(Color(0xFF2E1015), Color(0xFF161822), Color(0xFF0F1118))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xFF182236), Color(0xFF141724), Color(0xFF0C0E14))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (item.isVideo) CrimsonPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isVideo) Icons.Default.Movie else Icons.Default.Photo,
                    contentDescription = null,
                    tint = if (item.isVideo) CrimsonBright else TextSecondary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (item.isVideo) "Video Preview" else "Photo Preview",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${item.displayName}\n${item.formattedSize}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
