package com.optivax.app.presentation.trash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.optivax.app.core.FileUtils
import com.optivax.app.core.rememberThumbnailState
import com.optivax.app.domain.model.TrashItem
import com.optivax.app.presentation.components.OptivaGlassCard
import com.optivax.app.presentation.theme.OptivaColors

@Composable
fun TrashBinDialog(
    trashItems: List<TrashItem>,
    totalTrashBytes: Long,
    onRestoreItem: (TrashItem) -> Unit,
    onRestoreAll: () -> Unit,
    onPurgeItem: (TrashItem) -> Unit,
    onEmptyTrash: () -> Unit,
    onDismiss: () -> Unit
) {
    var showEmptyConfirm by remember { mutableStateOf(false) }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = {
                Text("Çöp Kutusunu Boşalt", color = OptivaColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Çöp kutusundaki tüm dosyalar (${trashItems.size} dosya, ${FileUtils.formatFileSize(totalTrashBytes)}) kalıcı olarak silinecektir. Bu işlem geri alınamaz.",
                    color = OptivaColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmptyConfirm = false
                        onEmptyTrash()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OptivaColors.Danger, contentColor = OptivaColors.CanvasBase)
                ) {
                    Text("Kalıcı Olarak Boşalt", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text("İptal", color = OptivaColors.TextMuted)
                }
            },
            containerColor = OptivaColors.SurfaceElevated
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, OptivaColors.BorderSubtle, RoundedCornerShape(24.dp)),
            color = OptivaColors.SurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(OptivaColors.WarningContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = OptivaColors.Warning,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Geri Dönüşüm Kutusu",
                                style = MaterialTheme.typography.titleLarge,
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Silinenler 30 gün boyunca güvenle korunur",
                                style = MaterialTheme.typography.labelSmall,
                                color = OptivaColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OptivaColors.SurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = OptivaColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Banner & Actions
                if (trashItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${trashItems.size} Dosya • ${FileUtils.formatFileSize(totalTrashBytes)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = OptivaColors.Warning,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onRestoreAll,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp), tint = OptivaColors.BrandPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tümünü Kurtar", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showEmptyConfirm = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OptivaColors.DangerContainer, contentColor = OptivaColors.Danger),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp), tint = OptivaColors.Danger)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Boşalt", fontSize = 11.sp, color = OptivaColors.Danger, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Items List
                Box(modifier = Modifier.weight(1f)) {
                    if (trashItems.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(OptivaColors.BrandPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = OptivaColors.BrandPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Geri Dönüşüm Kutusu Boş",
                                style = MaterialTheme.typography.titleMedium,
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Silinen tüm dosyalar güvende veya henüz hiçbir dosya silinmedi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OptivaColors.TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(trashItems, key = { it.id }) { item ->
                                TrashItemCard(
                                    item = item,
                                    onRestore = { onRestoreItem(item) },
                                    onPurge = { onPurgeItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashItemCard(
    item: TrashItem,
    onRestore: () -> Unit,
    onPurge: () -> Unit
) {
    val thumbnail by rememberThumbnailState(item.file, targetSize = 120)

    OptivaGlassCard(
        borderColor = OptivaColors.BorderSubtle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!,
                        contentDescription = item.fileName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (item.isVideo) OptivaColors.Warning else OptivaColors.BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.formattedSize} • ${item.daysRemainingUntilAutoDelete} gün kaldı",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.daysRemainingUntilAutoDelete <= 3) OptivaColors.Danger else OptivaColors.Warning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.originalPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = OptivaColors.TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action: Restore
            IconButton(
                onClick = onRestore,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(OptivaColors.BrandPrimaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Geri Yükle",
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Action: Purge Permanently
            IconButton(
                onClick = onPurge,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(OptivaColors.DangerContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Kalıcı Sil",
                    tint = OptivaColors.Danger,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
