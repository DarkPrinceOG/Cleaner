package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SmartCategory
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldKeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ResetScope

@Composable
fun ResetConfirmationDialog(
    scope: ResetScope,
    targetCategory: SmartCategory,
    affectedCount: Int,
    pendingDeleteCount: Int,
    isResetting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isFolderScope = scope == ResetScope.THIS_FOLDER
    val title = if (isFolderScope) {
        "Reset ${targetCategory.label} History?"
    } else {
        "Reset All Review History?"
    }

    AlertDialog(
        onDismissRequest = {
            if (!isResetting) onDismiss()
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CrimsonPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = CrimsonBright,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val scopeDescription = if (isFolderScope) {
                    "This will reset review history (KEEP and SKIP) for $affectedCount items in ${targetCategory.label}. These photos will reappear in your review queue for fresh consideration."
                } else {
                    "This will reset review history (KEEP and SKIP) across every category ($affectedCount items total), including filtered media. All unreviewed and kept items will become available in the review queue."
                }

                Text(
                    text = scopeDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Safety guarantee box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161922))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = EmeraldKeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Safety Guarantees",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldKeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Actual files on your device are NEVER deleted.\n• Pending deletions in your Review Queue ($pendingDeleteCount staged items) remain completely safe and untouched.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isResetting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_reset")
            ) {
                if (isResetting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resetting...")
                } else {
                    Text(if (isFolderScope) "Reset Folder History" else "Reset All History")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isResetting,
                modifier = Modifier.testTag("btn_cancel_reset")
            ) {
                Text(
                    text = "Cancel",
                    color = TextSecondary
                )
            }
        },
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}
