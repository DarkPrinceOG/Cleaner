package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import com.example.model.StorageStats
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldKeep
import com.example.ui.theme.SkyPhotos
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StorageGauge(
    stats: StorageStats,
    modifier: Modifier = Modifier
) {
    val animatedPercent = remember { Animatable(0f) }

    LaunchedEffect(stats.deviceUsedPercent) {
        animatedPercent.animateTo(
            targetValue = stats.deviceUsedPercent,
            animationSpec = tween(durationMillis = 900)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceElevated)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Device Storage",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${MediaItem.formatBytes(stats.freeDeviceBytes)} available of ${MediaItem.formatBytes(stats.totalDeviceBytes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Percent Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (stats.deviceUsedPercent > 0.85f) CrimsonPrimary.copy(alpha = 0.2f)
                            else DarkBorder
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${(stats.deviceUsedPercent * 100).toInt()}% USED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (stats.deviceUsedPercent > 0.85f) CrimsonBright else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Linear segmented gauge bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                val width = size.width
                val height = size.height

                // Background track
                drawRect(color = Color(0xFF222633), size = size)

                // Photos segment (sky blue)
                val photoRatio = if (stats.totalDeviceBytes > 0) {
                    (stats.photosBytes.toFloat() / stats.totalDeviceBytes.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val photoWidth = width * photoRatio

                // Videos segment (crimson primary)
                val videoRatio = if (stats.totalDeviceBytes > 0) {
                    (stats.videosBytes.toFloat() / stats.totalDeviceBytes.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val videoWidth = width * videoRatio

                // Other used
                val otherUsedWidth = ((width * animatedPercent.value) - photoWidth - videoWidth).coerceAtLeast(0f)

                // Draw segments
                drawRect(
                    color = SkyPhotos,
                    topLeft = Offset.Zero,
                    size = Size(photoWidth, height)
                )
                drawRect(
                    color = CrimsonPrimary,
                    topLeft = Offset(photoWidth, 0f),
                    size = Size(videoWidth, height)
                )
                drawRect(
                    color = Color(0xFF6B7280),
                    topLeft = Offset(photoWidth + videoWidth, 0f),
                    size = Size(otherUsedWidth, height)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(
                    color = SkyPhotos,
                    label = "Photos",
                    sizeText = MediaItem.formatBytes(stats.photosBytes)
                )
                LegendItem(
                    color = CrimsonPrimary,
                    label = "Videos",
                    sizeText = MediaItem.formatBytes(stats.videosBytes)
                )
                LegendItem(
                    color = Color(0xFF6B7280),
                    label = "System/Apps",
                    sizeText = MediaItem.formatBytes((stats.usedDeviceBytes - stats.photosBytes - stats.videosBytes).coerceAtLeast(0))
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    sizeText: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = sizeText,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}
