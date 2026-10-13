package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.CategoryStat
import com.example.model.SmartCategory
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SmartCategoryBar(
    selectedCategory: SmartCategory,
    categoryStats: Map<SmartCategory, CategoryStat>,
    onSelectCategory: (SmartCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = SmartCategory.entries,
            key = { it.id }
        ) { category ->
            val isSelected = category == selectedCategory
            val stat = categoryStats[category]
            val unreviewedCount = stat?.unreviewedCount

            CategoryPill(
                category = category,
                isSelected = isSelected,
                unreviewedCount = unreviewedCount,
                onClick = { onSelectCategory(category) }
            )
        }
    }
}

@Composable
private fun CategoryPill(
    category: SmartCategory,
    isSelected: Boolean,
    unreviewedCount: Int?,
    onClick: () -> Unit
) {
    val icon = getCategoryIcon(category)

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) CrimsonPrimary else DarkSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) CrimsonBright else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("category_pill_${category.id}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White else TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )

            if (unreviewedCount != null && unreviewedCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else CrimsonPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = unreviewedCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else CrimsonBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: SmartCategory): ImageVector {
    return when (category) {
        SmartCategory.ALL_MEDIA -> Icons.Default.PhotoLibrary
        SmartCategory.CAMERA -> Icons.Default.CameraAlt
        SmartCategory.SCREENSHOTS -> Icons.Default.Screenshot
        SmartCategory.DOWNLOADS -> Icons.Default.Download
        SmartCategory.WHATSAPP -> Icons.Default.Chat
        SmartCategory.OTHER_IMAGES -> Icons.Default.Image
        SmartCategory.VIDEOS -> Icons.Default.Videocam
    }
}
