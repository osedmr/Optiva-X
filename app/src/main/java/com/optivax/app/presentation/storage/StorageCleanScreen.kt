package com.optivax.app.presentation.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.media.MediaPlayer
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import com.optivax.app.core.rememberFullMediaState
import com.optivax.app.core.rememberThumbnailState
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import com.optivax.app.domain.model.DownloadApkItem
import com.optivax.app.domain.model.DownloadType
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.optivax.app.core.Constants
import com.optivax.app.core.FileUtils
import com.optivax.app.domain.model.BlurryPhotoItem
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.DuplicateFileDetail
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.model.LargeFileItem
import com.optivax.app.domain.model.ScreenshotItem
import com.optivax.app.domain.model.SimilarPhotoGroup
import com.optivax.app.domain.model.TelegramCategoryType
import com.optivax.app.domain.model.TelegramMediaCategory
import com.optivax.app.domain.model.WhatsAppCategoryType
import com.optivax.app.domain.model.WhatsAppMediaCategory
import com.optivax.app.presentation.components.OptivaButton
import com.optivax.app.presentation.components.OptivaGlassCard
import com.optivax.app.presentation.components.RadarScanAnimation
import com.optivax.app.presentation.trash.TrashBinDialog
import com.optivax.app.presentation.theme.CyberAmber
import com.optivax.app.presentation.theme.CyberPurple
import com.optivax.app.presentation.theme.DarkSurface
import com.optivax.app.presentation.theme.DarkSurfaceCard
import com.optivax.app.presentation.theme.DarkSurfaceVariant
import com.optivax.app.presentation.theme.DeepObsidian
import com.optivax.app.presentation.theme.GlassBorder
import com.optivax.app.presentation.theme.NeonCoral
import com.optivax.app.presentation.theme.NeonCyan
import com.optivax.app.presentation.theme.NeonEmerald
import com.optivax.app.presentation.theme.OutlineDark
import com.optivax.app.presentation.theme.TextMuted
import com.optivax.app.presentation.theme.TextPrimary
import com.optivax.app.presentation.theme.TextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StorageCleanScreen(
    viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    // 1. Live Cleaning Progress Dialog
    if (uiState.isCleaning && uiState.cleaningProgress != null) {
        CleanProgressDialog(progress = uiState.cleaningProgress!!)
    }

    // 2. WhatsApp Category File Inspection Dialog
    if (uiState.inspectingCategory != null) {
        val category = uiState.inspectingCategory!!
        val selectedPaths = uiState.getSelectedFilesForCategory(category)
        WhatsAppCategoryInspectionDialog(
            category = category,
            selectedFilePaths = selectedPaths,
            onToggleFile = { viewModel.toggleFileInWhatsAppCategory(category.type, it) },
            onSelectAll = { viewModel.selectAllFilesInWhatsAppCategory(category.type) },
            onDeselectAll = { viewModel.deselectAllFilesInWhatsAppCategory(category.type) },
            onDismiss = { viewModel.closeCategoryInspection() },
            onOpenFile = { openFileWithSystemApp(context, it) }
        )
    }

    // 2.5. Telegram Media Category Inspection Dialog
    if (uiState.inspectingTelegramCategory != null) {
        val cat = uiState.inspectingTelegramCategory!!
        TelegramCategoryInspectionDialog(
            category = cat,
            selectedFilePaths = uiState.getSelectedFilesForTelegramCategory(cat),
            onToggleFile = { viewModel.toggleFileInTelegramCategory(cat.type, it) },
            onSelectAll = { viewModel.selectAllFilesInTelegramCategory(cat.type) },
            onDeselectAll = { viewModel.deselectAllFilesInTelegramCategory(cat.type) },
            onDismiss = { viewModel.closeTelegramCategoryInspection() },
            onOpenFile = { openFileWithSystemApp(context, it) }
        )
    }

    // 2.8. Safe Trash Bin Dialog
    if (uiState.isTrashDialogVisible) {
        TrashBinDialog(
            trashItems = uiState.trashItems,
            totalTrashBytes = uiState.totalTrashBytes,
            onRestoreItem = { viewModel.restoreTrashItem(it) },
            onRestoreAll = { viewModel.restoreAllTrash() },
            onPurgeItem = { viewModel.purgeTrashItem(it) },
            onEmptyTrash = { viewModel.emptyTrash() },
            onDismiss = { viewModel.closeTrashDialog() }
        )
    }

    // 3. Pre-cleaning Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = if (uiState.useTrashBin) "Güvenli Çöp Kutusuna Taşı" else "Kalıcı Olarak Temizle",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "${FileUtils.formatFileSize(uiState.selectedBytesToClean)} boyutundaki seçilen dosyalar temizlenecektir.",
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OptivaGlassCard(
                        borderColor = CyberAmber.copy(alpha = 0.5f),
                        backgroundColor = DarkSurfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "30 Günlük Güvenli Geri Dönüşüm",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dosyalar çöp kutusunda tutulur, dilediğiniz zaman geri yükleyebilirsiniz.",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = uiState.useTrashBin,
                                onCheckedChange = { viewModel.toggleUseTrashBin(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DeepObsidian,
                                    checkedTrackColor = CyberAmber
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.cleanSelected()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCoral, contentColor = DeepObsidian)
                ) {
                    Text(if (uiState.useTrashBin) "Çöp Kutusuna Taşı" else "Kalıcı Olarak Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 4. Post-cleaning Celebration Dialog
    if (uiState.cleanedBytesResult != null && (uiState.cleanedBytesResult ?: 0L) > 0L) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCleanResult() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Temizlik Tamamlandı!",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "Cihazınızda ${FileUtils.formatFileSize(uiState.cleanedBytesResult ?: 0L)} boş alan başarıyla geri kazanıldı. Sohbetleriniz güvende tutuldu.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCleanResult() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = DeepObsidian)
                ) {
                    Text("Harika!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepObsidian
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Depolama Temizliği",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "SHA-256 çift dosya, WhatsApp ve kalıntı eliminasyonu",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Safe Trash Bin Button with Badge
                        IconButton(
                            onClick = { viewModel.openTrashDialog() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberAmber.copy(alpha = 0.12f))
                        ) {
                            if (uiState.trashItems.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = NeonCoral,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (uiState.trashItems.size > 99) "99+" else "${uiState.trashItems.size}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Geri Dönüşüm Kutusu",
                                        tint = CyberAmber
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Geri Dönüşüm Kutusu",
                                    tint = CyberAmber
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.startScan() },
                            enabled = !uiState.isScanning && !uiState.isCleaning,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonCyan.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yeniden Tara",
                                tint = NeonCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3-Power Hubs Main Category Selector (Sosyal Medya, Galeri AI, Dosya Avcısı)
                HubSelectorBar(
                    activeHub = uiState.activeHub,
                    socialCount = uiState.socialItemCount,
                    galleryCount = uiState.galleryItemCount,
                    systemCount = uiState.systemItemCount,
                    onHubSelect = { viewModel.switchHub(it) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Focused Sub-Chips for the Active Hub
                SubHubChipsRow(
                    activeHub = uiState.activeHub,
                    activeTab = uiState.activeTab,
                    duplicateCount = uiState.duplicates.size,
                    whatsAppCount = uiState.whatsAppCategories.size,
                    telegramCount = uiState.telegramCategories.size,
                    similarCount = uiState.similarPhotoGroups.size,
                    blurryCount = uiState.blurryPhotos.size,
                    largeFilesCount = uiState.largeFiles.size,
                    screenshotsCount = uiState.screenshots.size,
                    corpseCount = uiState.corpseFiles.size,
                    downloadsCount = uiState.downloadsAndApks.size,
                    onTabSelect = { viewModel.switchTab(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Selection Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (uiState.activeTab) {
                        StorageCleanTab.DUPLICATES -> {
                            TextButton(onClick = { viewModel.selectAllDuplicatesSuggested() }) {
                                Text("Önerilen Kopyaları Seç", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllDuplicates() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.WHATSAPP -> {
                            TextButton(onClick = { viewModel.selectAllWhatsAppCategories() }) {
                                Text("Tümünü Seç", color = NeonEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllWhatsAppCategories() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.TELEGRAM -> {
                            TextButton(onClick = { viewModel.selectAllTelegramCategories() }) {
                                Text("Tümünü Seç", color = Color(0xFF00B0FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllTelegramCategories() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.SIMILAR_PHOTOS -> {
                            TextButton(onClick = { viewModel.selectAllSimilarPhotosSuggested() }) {
                                Text("Önerilen Kopyaları Seç", color = Color(0xFFE040FB), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllSimilarPhotos() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.BLURRY_PHOTOS -> {
                            TextButton(onClick = { viewModel.selectAllBlurryPhotos() }) {
                                Text("Tüm Bulanıkları Seç", color = CyberAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllBlurryPhotos() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.LARGE_FILES -> {
                            TextButton(onClick = { viewModel.selectAllLargeFiles() }) {
                                Text("Tümünü Seç", color = NeonCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllLargeFiles() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.SCREENSHOTS -> {
                            TextButton(onClick = { viewModel.selectAllScreenshots() }) {
                                Text("Tümünü Seç", color = CyberPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllScreenshots() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.CORPSE_FILES -> {
                            TextButton(onClick = { viewModel.selectAllCorpseFiles() }) {
                                Text("Tüm Artıkları Seç", color = CyberAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.deselectAllCorpseFiles() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        StorageCleanTab.DOWNLOADS_APKS -> {
                            Row {
                                TextButton(onClick = { viewModel.selectAllInstalledApksSuggested() }) {
                                    Text("Kurulu APK'ları Seç", color = NeonEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                TextButton(onClick = { viewModel.selectAllDownloads() }) {
                                    Text("Tümünü Seç", color = NeonCyan, fontSize = 12.sp)
                                }
                            }
                            TextButton(onClick = { viewModel.deselectAllDownloads() }) {
                                Text("Temizle", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Main Content Area
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.isScanning) {
                        RadarScanAnimation(
                            modifier = Modifier.fillMaxSize(),
                            statusText = "Derin Dosya ve Medya Analizi..."
                        )
                    } else {
                        when (uiState.activeTab) {
                            StorageCleanTab.DUPLICATES -> {
                                if (uiState.duplicates.isEmpty()) {
                                    EmptyStateView(
                                        title = "Çift Dosya Bulunmadı",
                                        subtitle = "Cihazınızda SHA-256 sağlama değeri eşleşen gereksiz kopya bulunmamaktadır."
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 20.dp),
                                        contentPadding = PaddingValues(bottom = 220.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        items(uiState.duplicates, key = { it.sha256Hash }) { item ->
                                            DuplicateGroupCard(
                                                group = item,
                                                selectedPaths = uiState.selectedDuplicatePaths,
                                                onToggle = { viewModel.toggleDuplicateSelection(it) }
                                            )
                                        }
                                    }
                                }
                            }
                            StorageCleanTab.WHATSAPP -> {
                                if (uiState.whatsAppCategories.isEmpty()) {
                                    EmptyStateView(
                                        title = "WhatsApp Medyası Temiz",
                                        subtitle = "WhatsApp klasörlerinizde gönderilen medya kopyası veya birikmiş kalıntı bulunamadı."
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 20.dp),
                                        contentPadding = PaddingValues(bottom = 220.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        item {
                                            WhatsAppHeroCard(
                                                totalBytes = uiState.totalWhatsAppBytes,
                                                categoryCount = uiState.whatsAppCategories.size
                                            )
                                        }

                                        items(uiState.whatsAppCategories, key = { it.type.name }) { category ->
                                            val selectedFiles = uiState.getSelectedFilesForCategory(category)
                                            val isFullyOrPartiallySelected = selectedFiles.isNotEmpty()
                                            WhatsAppCategoryCard(
                                                category = category,
                                                selectedCount = selectedFiles.size,
                                                isSelected = isFullyOrPartiallySelected,
                                                onToggle = { viewModel.toggleWhatsAppCategory(category.type) },
                                                onInspect = { viewModel.openCategoryInspection(category) }
                                            )
                                        }
                                    }
                                }
                            }
                            StorageCleanTab.TELEGRAM -> {
                                TelegramTabContent(
                                    categories = uiState.telegramCategories,
                                    selectedCategories = uiState.selectedTelegramCategories,
                                    totalBytes = uiState.totalTelegramBytes,
                                    onToggleCategory = { viewModel.toggleTelegramCategory(it) },
                                    onInspectCategory = { viewModel.openTelegramCategoryInspection(it) },
                                    uiState = uiState
                                )
                            }
                            StorageCleanTab.SIMILAR_PHOTOS -> {
                                SimilarPhotosView(
                                    groups = uiState.similarPhotoGroups,
                                    selectedPaths = uiState.selectedSimilarPhotoPaths,
                                    totalSavingsBytes = uiState.totalSimilarPhotosSavingsBytes,
                                    onToggleFile = { viewModel.toggleSimilarPhotoSelection(it) },
                                    onOpenFile = { openFileWithSystemApp(context, it) }
                                )
                            }
                            StorageCleanTab.BLURRY_PHOTOS -> {
                                BlurryPhotosView(
                                    blurryPhotos = uiState.blurryPhotos,
                                    selectedPaths = uiState.selectedBlurryPhotoPaths,
                                    totalBytes = uiState.totalBlurryBytes,
                                    currentThreshold = uiState.blurryThreshold,
                                    onSelectThreshold = { viewModel.setBlurryThreshold(it) },
                                    onToggleFile = { viewModel.toggleBlurryPhotoSelection(it) },
                                    onOpenFile = { openFileWithSystemApp(context, it) }
                                )
                            }
                            StorageCleanTab.LARGE_FILES -> {
                                LargeFilesView(
                                    largeFiles = uiState.largeFiles,
                                    selectedPaths = uiState.selectedLargeFilePaths,
                                    currentThresholdBytes = uiState.largeFileSizeThreshold,
                                    onSelectThreshold = { viewModel.setLargeFileSizeThreshold(it) },
                                    onToggleFile = { viewModel.toggleLargeFileSelection(it) },
                                    onOpenFile = { openFileWithSystemApp(context, it) }
                                )
                            }
                            StorageCleanTab.SCREENSHOTS -> {
                                ScreenshotsView(
                                    screenshots = uiState.screenshots,
                                    selectedPaths = uiState.selectedScreenshotPaths,
                                    currentAgeDays = uiState.screenshotAgeThresholdDays,
                                    onSelectAgeDays = { viewModel.setScreenshotAgeThreshold(it) },
                                    onToggleFile = { viewModel.toggleScreenshotSelection(it) },
                                    onOpenFile = { openFileWithSystemApp(context, it) }
                                )
                            }
                            StorageCleanTab.CORPSE_FILES -> {
                                if (uiState.corpseFiles.isEmpty()) {
                                    EmptyStateView(
                                        title = "Kalıntı Dosya Bulunmadı",
                                        subtitle = "Cihazınızda kaldırılmış uygulamalara veya önizleme önbelleklerine ait artık veri yok."
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 20.dp),
                                        contentPadding = PaddingValues(bottom = 220.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(uiState.corpseFiles, key = { it.path }) { corpse ->
                                            CorpseItemCard(
                                                item = corpse,
                                                isSelected = uiState.selectedCorpseItems.contains(corpse),
                                                onToggle = { viewModel.toggleCorpseSelection(corpse) }
                                            )
                                        }
                                    }
                                }
                            }
                            StorageCleanTab.DOWNLOADS_APKS -> {
                                if (uiState.downloadsAndApks.isEmpty()) {
                                    EmptyStateView(
                                        title = "İndirilenler & APK Temiz",
                                        subtitle = "İndirilenler klasörünüzde unutulmuş dosya veya eski APK yükleme paketi bulunamadı."
                                    )
                                } else {
                                    DownloadsAndApksView(
                                        items = uiState.downloadsAndApks,
                                        selectedPaths = uiState.selectedDownloadPaths,
                                        activeFilter = uiState.downloadFilterType,
                                        onSelectFilter = { viewModel.setDownloadFilterType(it) },
                                        onToggleItem = { viewModel.toggleDownloadSelection(it) },
                                        onOpenFile = { file -> openFileWithSystemApp(context, file) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Clean Recovery Action Bar (Elevated above bottom navigation bar: bottom = 90.dp with navigationBarsPadding)
            if (uiState.selectedBytesToClean > 0L && !uiState.isScanning && !uiState.isCleaning) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 90.dp)
                ) {
                    OptivaGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonCoral.copy(alpha = 0.85f),
                        backgroundColor = DarkSurfaceCard.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kurtarılacak Alan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = FileUtils.formatFileSize(uiState.selectedBytesToClean),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = NeonCoral,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            OptivaButton(
                                text = "Temizle",
                                icon = Icons.Default.CleaningServices,
                                onClick = { showConfirmDialog = true },
                                isLoading = uiState.isCleaning,
                                containerColor = NeonCoral,
                                contentColor = DeepObsidian,
                                modifier = Modifier.width(136.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Live Clean Progress Dialog displaying real-time files deleted, percentage, and current file name.
 */
@Composable
private fun CleanProgressDialog(progress: CleanProgress) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.2.dp, NeonEmerald.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            color = DarkSurfaceCard.copy(alpha = 0.96f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Güvenle Temizleniyor...",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Lütfen bekleyin, seçilen dosyalar siliniyor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Progress Indicator
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.progressPercentage,
                    animationSpec = tween(durationMillis = 200),
                    label = "cleanProgress"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = NeonEmerald,
                    trackColor = DarkSurfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${progress.current} / ${progress.total} dosya",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress.progressPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kurtarılan: ${progress.formattedFreed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )

                if (progress.currentFileName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = progress.currentFileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * WhatsApp In-App Media Gallery & Inspection Dialog
 * Displays media in a 3-column square grid (like WhatsApp Media Gallery) with real thumbnails,
 * inline selection checkboxes, and full-screen in-app preview!
 */
@Composable
private fun WhatsAppCategoryInspectionDialog(
    category: WhatsAppMediaCategory,
    selectedFilePaths: Set<String>,
    onToggleFile: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDismiss: () -> Unit,
    onOpenFile: (File) -> Unit
) {
    val selectedFiles = category.files.filter { selectedFilePaths.contains(it.absolutePath) }
    val selectedBytes = selectedFiles.sumOf { it.length() }

    var isGridView by remember { mutableStateOf(category.type.isVisualMedia) }
    var previewFile by remember { mutableStateOf<File?>(null) }

    // In-App Fullscreen Media Viewer
    if (previewFile != null) {
        InAppMediaViewerDialog(
            file = previewFile!!,
            isSelected = selectedFilePaths.contains(previewFile!!.absolutePath),
            onToggleSelection = { onToggleFile(previewFile!!.absolutePath) },
            onDismiss = { previewFile = null },
            onOpenExternally = { onOpenFile(previewFile!!) }
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
                .border(1.2.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = DarkSurfaceCard.copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedFiles.size} / ${category.fileCount} seçili (${FileUtils.formatFileSize(selectedBytes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // View switcher: Grid <-> List
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = "Görünüm Değiştir",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Select / Deselect All Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tümünü Seç", fontSize = 12.sp, color = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = onDeselectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Seçimi Kaldır", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Main Content (Grid vs List)
                Box(modifier = Modifier.weight(1f)) {
                    if (isGridView) {
                        // 3-Column Square Media Gallery Grid (WhatsApp style)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(category.files, key = { it.absolutePath }) { file ->
                                val isChecked = selectedFilePaths.contains(file.absolutePath)
                                WhatsAppMediaGridTile(
                                    file = file,
                                    isChecked = isChecked,
                                    onToggle = { onToggleFile(file.absolutePath) },
                                    onPreview = { previewFile = file }
                                )
                            }
                        }
                    } else {
                        // Detailed List View (with audio badges for voice notes)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(category.files, key = { it.absolutePath }) { file ->
                                val isChecked = selectedFilePaths.contains(file.absolutePath)
                                WhatsAppMediaListRow(
                                    file = file,
                                    isChecked = isChecked,
                                    onToggle = { onToggleFile(file.absolutePath) },
                                    onPreview = { previewFile = file }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm and apply selection
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = DeepObsidian),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Seçimi Onayla (${selectedFiles.size} Dosya)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Square Tile in WhatsApp Gallery style with thumbnail, selection checkmark overlay, and zoom button.
 */
@Composable
private fun WhatsAppMediaGridTile(
    file: File,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit
) {
    val ext = file.extension.lowercase()
    val isAudio = ext in listOf("opus", "m4a", "aac", "mp3", "ogg", "wav", "amr", "flac")
    val isVideo = ext in listOf("mp4", "3gp", "mkv", "mov", "webm", "avi")
    val isVoiceNote = file.name.startsWith("PTT-") || file.name.contains("voice", ignoreCase = true)
    val thumbnail by rememberThumbnailState(file, targetSize = 220)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isAudio) Color(0xFF0D221A) else DarkSurfaceVariant)
            .border(
                width = if (isChecked) 2.dp else 0.5.dp,
                color = if (isChecked) NeonCoral else if (isAudio) NeonEmerald.copy(alpha = 0.4f) else GlassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
    ) {
        if (isAudio) {
            // Audio & Voice Note Tile
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVoiceNote) Icons.Default.Mic else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isVoiceNote) "Sesli Mesaj" else ".${ext.uppercase()}",
                    color = NeonEmerald,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        } else if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!,
                contentDescription = file.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Image,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Darkened Scrim when selected
        if (isChecked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.38f))
            )
        }

        // Selection Checkmark (Top-Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isChecked) NeonCoral else Color.Black.copy(alpha = 0.45f))
                .border(1.5.dp, if (isChecked) NeonCoral else Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçildi",
                    tint = DeepObsidian,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // In-App Preview / Play Button (Top-Left)
        IconButton(
            onClick = onPreview,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isAudio) NeonEmerald else Color.Black.copy(alpha = 0.55f))
        ) {
            Icon(
                imageVector = if (isAudio) Icons.Default.PlayArrow else Icons.Default.ZoomIn,
                contentDescription = if (isAudio) "Dinle" else "Büyüt",
                tint = if (isAudio) DeepObsidian else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Bottom Scrim Bar (File size & type badge)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isVideo) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                } else if (isAudio) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Text(
                    text = FileUtils.formatFileSize(file.length()),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatAudioTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

/**
 * Dedicated In-App Cyberpunk Audio Player for WhatsApp Voice Notes (.opus) and Audio files (.mp3, .m4a, etc.).
 */
@Composable
private fun InAppAudioPlayer(
    file: File,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0) }
    var durationMs by remember { mutableStateOf(1) }
    var isPrepared by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val mediaPlayer = remember { MediaPlayer() }
    val isVoiceNote = file.name.startsWith("PTT-") || file.name.contains("voice", ignoreCase = true) || file.extension.lowercase() == "opus"

    DisposableEffect(file) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.setOnPreparedListener { mp ->
                isPrepared = true
                durationMs = if (mp.duration > 0) mp.duration else 1
                try {
                    mp.start()
                    isPlaying = true
                } catch (e: Exception) {
                    // ignore
                }
            }
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                currentPositionMs = durationMs
            }
            mediaPlayer.setOnErrorListener { _, what, extra ->
                errorMessage = "Ses oynatılamadı ($what, $extra)"
                isPlaying = false
                true
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            errorMessage = "Ses dosyası yüklenemedi: ${e.message}"
        }

        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            try {
                if (mediaPlayer.isPlaying) {
                    currentPositionMs = mediaPlayer.currentPosition
                }
            } catch (e: Exception) {
                // ignore
            }
            kotlinx.coroutines.delay(100)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Audio Avatar / Microphone Icon
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(NeonEmerald.copy(alpha = 0.12f))
                .border(2.dp, NeonEmerald.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isVoiceNote) Icons.Default.Mic else Icons.Default.Audiotrack,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isVoiceNote) "WhatsApp Sesli Mesajı" else "Ses Dosyası",
            style = MaterialTheme.typography.titleMedium,
            color = NeonEmerald,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Cyberpunk Animated Waveform Equalizer
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val progressRatio = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            val barHeights = listOf(14, 24, 38, 44, 28, 40, 18, 32, 44, 22, 36, 42, 26, 38, 16, 30, 44, 20, 34, 18)
            barHeights.forEachIndexed { index, baseHeight ->
                val barProgress = index.toFloat() / barHeights.size.toFloat()
                val isPlayed = barProgress <= progressRatio
                val animatedHeight = if (isPlaying) {
                    val variation = ((index * 7 + (currentPositionMs / 100)) % 22) - 11
                    (baseHeight + variation).coerceIn(8, 44).dp
                } else {
                    baseHeight.dp
                }
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(animatedHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isPlayed) NeonEmerald else DarkSurfaceVariant.copy(alpha = 0.8f))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = NeonCoral,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        } else {
            // Interactive Seekbar
            Slider(
                value = currentPositionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                onValueChange = { newPos ->
                    currentPositionMs = newPos.toInt()
                    if (isPrepared) {
                        try {
                            mediaPlayer.seekTo(newPos.toInt())
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                },
                valueRange = 0f..durationMs.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonEmerald,
                    activeTrackColor = NeonEmerald,
                    inactiveTrackColor = DarkSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Current Time / Total Duration Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatAudioTime(currentPositionMs),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatAudioTime(durationMs),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playback Controls Row (-5s, Play/Pause, +5s)
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newPos = (currentPositionMs - 5000).coerceAtLeast(0)
                    mediaPlayer.seekTo(newPos)
                    currentPositionMs = newPos
                },
                enabled = isPrepared,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Text("-5s", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(NeonEmerald)
                    .clickable(enabled = isPrepared) {
                        try {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                                isPlaying = false
                            } else {
                                mediaPlayer.start()
                                isPlaying = true
                            }
                        } catch (e: Exception) {
                            errorMessage = "Oynatma hatası: ${e.message}"
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Durdur" else "Oynat",
                    tint = DeepObsidian,
                    modifier = Modifier.size(38.dp)
                )
            }

            IconButton(
                onClick = {
                    val newPos = (currentPositionMs + 5000).coerceAtMost(durationMs)
                    mediaPlayer.seekTo(newPos)
                    currentPositionMs = newPos
                },
                enabled = isPrepared,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Text("+5s", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Dedicated In-App Cyberpunk Video Player with embedded controls.
 */
@Composable
private fun InAppVideoPlayer(
    file: File,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoPath(file.absolutePath)
                    val mediaController = MediaController(ctx)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        start()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * In-App Fullscreen Media Viewer Dialog.
 * Shows high-resolution photo, dedicated Audio Player, or Video Player directly within the app!
 */
@Composable
private fun InAppMediaViewerDialog(
    file: File,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onDismiss: () -> Unit,
    onOpenExternally: () -> Unit
) {
    val ext = file.extension.lowercase()
    val isAudio = ext in listOf("opus", "m4a", "aac", "mp3", "ogg", "wav", "amr", "flac")
    val isVideo = ext in listOf("mp4", "3gp", "mkv", "mov", "webm", "avi")
    val isImage = ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

    val fullBitmap by rememberFullMediaState(file)
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DeepObsidian.copy(alpha = 0.98f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${FileUtils.formatFileSize(file.length())} • ${dateFormat.format(Date(file.lastModified()))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = onOpenExternally,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Dışarıda Aç",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Main Content Frame (Audio / Video / Image / Document)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isAudio -> {
                                InAppAudioPlayer(
                                    file = file,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            isVideo -> {
                                InAppVideoPlayer(
                                    file = file,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            isImage -> {
                                if (fullBitmap != null) {
                                    Image(
                                        bitmap = fullBitmap!!,
                                        contentDescription = file.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = NeonCyan)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Görsel Yükleniyor...", color = TextSecondary)
                                    }
                                }
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(DarkSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onOpenExternally,
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepObsidian),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Sistem Uygulaması ile Aç", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Action Bar: Toggle Delete Status
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Button(
                            onClick = { onToggleSelection() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCoral else NeonEmerald,
                                contentColor = DeepObsidian
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Delete else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSelected) "Silinecek Olarak Seçildi (Kaldırmak İçin Dokun)" else "Güvende Tutuluyor (Silmek İçin Seç)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detailed List Item for Audio, Documents, or list view mode.
 */
@Composable
private fun WhatsAppMediaListRow(
    file: File,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(file.lastModified()))
    val isAudio = file.extension.lowercase() in listOf("opus", "m4a", "aac", "mp3", "ogg", "wav", "amr", "flac")
    val isVoiceNote = file.name.startsWith("PTT-") || file.name.contains("voice", ignoreCase = true) || file.extension.lowercase() == "opus"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = NeonCoral, uncheckedColor = TextMuted)
        )
        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isAudio) NeonEmerald.copy(alpha = 0.18f) else NeonCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAudio) (if (isVoiceNote) Icons.Default.Mic else Icons.Default.Audiotrack) else Icons.Default.Description,
                contentDescription = null,
                tint = if (isAudio) NeonEmerald else NeonCyan,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = FileUtils.formatFileSize(file.length()),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isChecked) NeonCoral else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onPreview,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isAudio) NeonEmerald else NeonCyan.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = if (isAudio) Icons.Default.PlayArrow else Icons.Default.ZoomIn,
                contentDescription = if (isAudio) "Dinle" else "Önizle",
                tint = if (isAudio) DeepObsidian else NeonCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun openFileWithSystemApp(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e(Constants.TAG, "[PREVIEW] Cannot open file: ${file.path}, error: ${e.message}")
    }
}

@Composable
private fun HubSelectorBar(
    activeHub: StorageCleanHub,
    socialCount: Int,
    galleryCount: Int,
    systemCount: Int,
    onHubSelect: (StorageCleanHub) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HubCardItem(
            modifier = Modifier.weight(1f),
            title = "Sosyal",
            subtitle = "WhatsApp & TG",
            count = socialCount,
            isSelected = activeHub == StorageCleanHub.SOCIAL,
            activeColor = NeonEmerald,
            icon = Icons.Default.Forum,
            onClick = { onHubSelect(StorageCleanHub.SOCIAL) }
        )
        HubCardItem(
            modifier = Modifier.weight(1f),
            title = "Galeri AI",
            subtitle = "Benzer & Bulanık",
            count = galleryCount,
            isSelected = activeHub == StorageCleanHub.GALLERY,
            activeColor = CyberPurple,
            icon = Icons.Default.PhotoLibrary,
            onClick = { onHubSelect(StorageCleanHub.GALLERY) }
        )
        HubCardItem(
            modifier = Modifier.weight(1f),
            title = "Dosya Avcısı",
            subtitle = "Çiftler & APK",
            count = systemCount,
            isSelected = activeHub == StorageCleanHub.SYSTEM,
            activeColor = NeonCyan,
            icon = Icons.Default.FolderZip,
            onClick = { onHubSelect(StorageCleanHub.SYSTEM) }
        )
    }
}

@Composable
private fun HubCardItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    count: Int,
    isSelected: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val bgAnimColor by animateColorAsState(
        if (isSelected) activeColor.copy(alpha = 0.16f) else DarkSurfaceCard.copy(alpha = 0.75f),
        label = "hubBg"
    )
    val borderAnimColor by animateColorAsState(
        if (isSelected) activeColor else GlassBorder,
        label = "hubBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgAnimColor)
            .border(if (isSelected) 1.5.dp else 0.8.dp, borderAnimColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) activeColor else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                if (count > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) activeColor else DarkSurfaceVariant)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (count > 99) "99+" else "$count",
                            color = if (isSelected) DeepObsidian else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) activeColor.copy(alpha = 0.9f) else TextMuted,
                fontSize = 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SubHubChipsRow(
    activeHub: StorageCleanHub,
    activeTab: StorageCleanTab,
    duplicateCount: Int,
    whatsAppCount: Int,
    telegramCount: Int,
    similarCount: Int,
    blurryCount: Int,
    largeFilesCount: Int,
    screenshotsCount: Int,
    corpseCount: Int,
    downloadsCount: Int,
    onTabSelect: (StorageCleanTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (activeHub) {
            StorageCleanHub.SOCIAL -> {
                item {
                    SubFilterChip(
                        title = "WhatsApp ($whatsAppCount Kat.)",
                        isSelected = activeTab == StorageCleanTab.WHATSAPP,
                        activeColor = NeonEmerald,
                        onClick = { onTabSelect(StorageCleanTab.WHATSAPP) }
                    )
                }
                item {
                    SubFilterChip(
                        title = "Telegram ($telegramCount Kat.)",
                        isSelected = activeTab == StorageCleanTab.TELEGRAM,
                        activeColor = Color(0xFF00B0FF),
                        onClick = { onTabSelect(StorageCleanTab.TELEGRAM) }
                    )
                }
            }
            StorageCleanHub.GALLERY -> {
                item {
                    SubFilterChip(
                        title = "Benzer Fotoğraflar ($similarCount)",
                        isSelected = activeTab == StorageCleanTab.SIMILAR_PHOTOS,
                        activeColor = CyberPurple,
                        onClick = { onTabSelect(StorageCleanTab.SIMILAR_PHOTOS) }
                    )
                }
                item {
                    SubFilterChip(
                        title = "Bulanık Fotoğraflar ($blurryCount)",
                        isSelected = activeTab == StorageCleanTab.BLURRY_PHOTOS,
                        activeColor = CyberAmber,
                        onClick = { onTabSelect(StorageCleanTab.BLURRY_PHOTOS) }
                    )
                }
                item {
                    SubFilterChip(
                        title = "Ekran Görüntüleri ($screenshotsCount)",
                        isSelected = activeTab == StorageCleanTab.SCREENSHOTS,
                        activeColor = NeonCyan,
                        onClick = { onTabSelect(StorageCleanTab.SCREENSHOTS) }
                    )
                }
            }
            StorageCleanHub.SYSTEM -> {
                item {
                    SubFilterChip(
                        title = "Çift Dosyalar ($duplicateCount)",
                        isSelected = activeTab == StorageCleanTab.DUPLICATES,
                        activeColor = NeonCyan,
                        onClick = { onTabSelect(StorageCleanTab.DUPLICATES) }
                    )
                }
                item {
                    SubFilterChip(
                        title = "İndirilenler & APK ($downloadsCount)",
                        isSelected = activeTab == StorageCleanTab.DOWNLOADS_APKS,
                        activeColor = NeonEmerald,
                        onClick = { onTabSelect(StorageCleanTab.DOWNLOADS_APKS) }
                    )
                }
                item {
                    SubFilterChip(
                        title = "Büyük Dosyalar ($largeFilesCount)",
                        isSelected = activeTab == StorageCleanTab.LARGE_FILES,
                        activeColor = NeonCoral,
                        onClick = { onTabSelect(StorageCleanTab.LARGE_FILES) }
                    )
                }
                item {
                    SubFilterChip(
                        title = "Kalıntı Veriler ($corpseCount)",
                        isSelected = activeTab == StorageCleanTab.CORPSE_FILES,
                        activeColor = CyberAmber,
                        onClick = { onTabSelect(StorageCleanTab.CORPSE_FILES) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubFilterChip(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isSelected) activeColor else DarkSurfaceVariant.copy(alpha = 0.6f),
        label = "subChipBg"
    )
    val textColor by animateColorAsState(
        if (isSelected) DeepObsidian else TextSecondary,
        label = "subChipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun LargeFilesView(
    largeFiles: List<LargeFileItem>,
    selectedPaths: Set<String>,
    currentThresholdBytes: Long,
    onSelectThreshold: (Long) -> Unit,
    onToggleFile: (String) -> Unit,
    onOpenFile: (File) -> Unit
) {
    var previewFile by remember { mutableStateOf<File?>(null) }

    if (previewFile != null) {
        InAppMediaViewerDialog(
            file = previewFile!!,
            isSelected = selectedPaths.contains(previewFile!!.absolutePath),
            onToggleSelection = { onToggleFile(previewFile!!.absolutePath) },
            onDismiss = { previewFile = null },
            onOpenExternally = { onOpenFile(previewFile!!) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Filter Chips Row (>50MB, >100MB, >500MB)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                50L * 1024L * 1024L to "> 50 MB",
                100L * 1024L * 1024L to "> 100 MB",
                500L * 1024L * 1024L to "> 500 MB"
            ).forEach { (bytes, label) ->
                val isSelected = currentThresholdBytes == bytes
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonCoral else DarkSurfaceVariant)
                        .clickable { onSelectThreshold(bytes) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) DeepObsidian else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (largeFiles.isEmpty()) {
            EmptyStateView(
                title = "Büyük Dosya Bulunmadı",
                subtitle = "Seçili boyut kriterine (${FileUtils.formatFileSize(currentThresholdBytes)}+) uyan dosya yok."
            )
        } else {
            val totalSize = largeFiles.sumOf { it.sizeBytes }
            Text(
                text = "${largeFiles.size} Büyük Dosya • Toplam ${FileUtils.formatFileSize(totalSize)}",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 220.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(largeFiles, key = { it.path }) { item ->
                    val isChecked = selectedPaths.contains(item.path)
                    LargeFileCard(
                        item = item,
                        isChecked = isChecked,
                        onToggle = { onToggleFile(item.path) },
                        onPreview = { previewFile = item.file }
                    )
                }
            }
        }
    }
}

@Composable
private fun LargeFileCard(
    item: LargeFileItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit
) {
    val thumbnail by rememberThumbnailState(item.file, targetSize = 160)

    OptivaGlassCard(
        borderColor = if (isChecked) NeonCoral else GlassBorder,
        modifier = Modifier.clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = NeonCoral, uncheckedColor = TextMuted)
            )

            // Video / File Thumbnail Box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (item.isVideo) NeonCoral else NeonCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.path,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.formattedSize,
                        color = NeonCoral,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(item.lastModified)),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onPreview,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Önizle",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ScreenshotsView(
    screenshots: List<ScreenshotItem>,
    selectedPaths: Set<String>,
    currentAgeDays: Int,
    onSelectAgeDays: (Int) -> Unit,
    onToggleFile: (String) -> Unit,
    onOpenFile: (File) -> Unit
) {
    var previewFile by remember { mutableStateOf<File?>(null) }

    if (previewFile != null) {
        InAppMediaViewerDialog(
            file = previewFile!!,
            isSelected = selectedPaths.contains(previewFile!!.absolutePath),
            onToggleSelection = { onToggleFile(previewFile!!.absolutePath) },
            onDismiss = { previewFile = null },
            onOpenExternally = { onOpenFile(previewFile!!) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Filter Chips Row (Tümü, >14 gün, >30 gün)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to "Tümü",
                14 to "> 14 Günlük",
                30 to "> 30 Günlük"
            ).forEach { (days, label) ->
                val isSelected = currentAgeDays == days
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyberPurple else DarkSurfaceVariant)
                        .clickable { onSelectAgeDays(days) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (screenshots.isEmpty()) {
            EmptyStateView(
                title = "Ekran Görüntüsü Bulunmadı",
                subtitle = "Seçilen zaman dilimine ait ekran görüntüsü bulunamadı."
            )
        } else {
            val totalSize = screenshots.sumOf { it.sizeBytes }
            Text(
                text = "${screenshots.size} Ekran Görüntüsü • Toplam ${FileUtils.formatFileSize(totalSize)}",
                style = MaterialTheme.typography.labelMedium,
                color = CyberPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 3-Column Square Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(screenshots, key = { it.path }) { item ->
                    val isChecked = selectedPaths.contains(item.path)
                    ScreenshotGridTile(
                        item = item,
                        isChecked = isChecked,
                        onToggle = { onToggleFile(item.path) },
                        onPreview = { previewFile = item.file }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotGridTile(
    item: ScreenshotItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit
) {
    val thumbnail by rememberThumbnailState(item.file, targetSize = 220)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(
                width = if (isChecked) 2.dp else 0.5.dp,
                color = if (isChecked) CyberPurple else GlassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (isChecked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.38f))
            )
        }

        // Top-Right Selection Checkmark Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isChecked) CyberPurple else Color.Black.copy(alpha = 0.45f))
                .border(1.5.dp, if (isChecked) CyberPurple else Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçildi",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Top-Left Quick Zoom Button
        IconButton(
            onClick = onPreview,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
        ) {
            Icon(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = "Büyüt",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Bottom Scrim Bar (File size & formatted age)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.formattedAge,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.formattedSize,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun WhatsAppHeroCard(
    totalBytes: Long,
    categoryCount: Int
) {
    OptivaGlassCard(
        borderColor = NeonEmerald.copy(alpha = 0.5f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "WhatsApp Medya Hijyeni",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$categoryCount kategoride ${FileUtils.formatFileSize(totalBytes)} fazlalık",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Her kategorideki 'İncele' butonuna dokunarak dosyaları önizleyebilir, dinleyebilir ve dilediğiniz dosyaları seçimden çıkarabilirsiniz. Sohbetleriniz silinmez.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun WhatsAppCategoryCard(
    category: WhatsAppMediaCategory,
    selectedCount: Int,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onInspect: () -> Unit
) {
    val icon = when (category.type) {
        WhatsAppCategoryType.SENT_VIDEOS -> Icons.Default.Videocam
        WhatsAppCategoryType.SENT_IMAGES -> Icons.Default.Image
        WhatsAppCategoryType.VOICE_NOTES -> Icons.Default.Mic
        WhatsAppCategoryType.STICKERS -> Icons.Default.EmojiEmotions
        WhatsAppCategoryType.STATUS_CACHE -> Icons.Default.Visibility
        WhatsAppCategoryType.SENT_DOCUMENTS -> Icons.Default.Description
        WhatsAppCategoryType.SENT_AUDIO -> Icons.Default.Audiotrack
    }

    OptivaGlassCard(
        borderColor = if (isSelected) NeonEmerald.copy(alpha = 0.6f) else OutlineDark
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = NeonEmerald, uncheckedColor = TextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonEmerald.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (category.isRecommendedClean) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Önerilen",
                                    color = NeonEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${category.formattedSize} • $selectedCount/${category.fileCount} seçili",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inspect & View files button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dosyaları İncele (${category.fileCount})",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DuplicateGroupCard(
    group: DuplicateItem,
    selectedPaths: Set<String>,
    onToggle: (String) -> Unit
) {
    OptivaGlassCard(borderColor = CyberPurple.copy(alpha = 0.35f)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberPurple.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = CyberPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${group.duplicates.size} Kopya Eşleşmesi",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dosya Boyutu: ${group.formattedSize}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCoral.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "İsraf: ${group.formattedWastedSize}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                group.duplicates.forEach { detail ->
                    val isChecked = selectedPaths.contains(detail.filePath)
                    DuplicateFileRow(
                        detail = detail,
                        isChecked = isChecked,
                        onCheckedChange = { onToggle(detail.filePath) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DuplicateFileRow(
    detail: DuplicateFileDetail,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.4f))
            .clickable { onCheckedChange() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(checkedColor = NeonCyan, uncheckedColor = TextMuted)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detail.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (detail.isOriginal) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Orijinal", style = MaterialTheme.typography.labelSmall, color = NeonEmerald, fontSize = 9.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCoral.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Kopya", style = MaterialTheme.typography.labelSmall, color = NeonCoral, fontSize = 9.sp)
                    }
                }
            }
            Text(
                text = detail.filePath,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CorpseItemCard(
    item: CorpseItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    OptivaGlassCard(
        borderColor = if (isSelected) NeonCoral.copy(alpha = 0.6f) else OutlineDark,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = NeonCoral, uncheckedColor = TextMuted)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCoral.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = NeonCoral,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.packageName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.fileCount} dosya • ${item.formattedSize}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonCoral,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.path,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NeonEmerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TelegramHeroCard(
    totalBytes: Long,
    categoryCount: Int
) {
    val electricBlue = Color(0xFF00B0FF)
    OptivaGlassCard(
        borderColor = electricBlue.copy(alpha = 0.5f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(electricBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = electricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Telegram Medya Hijyeni",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$categoryCount kategoride ${FileUtils.formatFileSize(totalBytes)} fazlalık",
                        style = MaterialTheme.typography.bodySmall,
                        color = electricBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kanallardan ve sohbetlerden otomatik indirilen büyük video ve belgeleri inceleyebilir, kare galeride görerek dilediklerinizi güvenle temizleyebilirsiniz.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TelegramCategoryCard(
    category: TelegramMediaCategory,
    selectedCount: Int,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onInspect: () -> Unit
) {
    val electricBlue = Color(0xFF00B0FF)
    val icon = when (category.type) {
        TelegramCategoryType.VIDEOS -> Icons.Default.Videocam
        TelegramCategoryType.IMAGES -> Icons.Default.Image
        TelegramCategoryType.DOCUMENTS -> Icons.Default.Description
        TelegramCategoryType.AUDIO -> Icons.Default.Audiotrack
        TelegramCategoryType.STORIES -> Icons.Default.AutoAwesome
    }

    OptivaGlassCard(
        borderColor = if (isSelected) electricBlue else GlassBorder,
        modifier = Modifier.clickable { onToggle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = electricBlue,
                        uncheckedColor = TextMuted
                    )
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(electricBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = electricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${category.fileCount} dosya • ${category.formattedSize}",
                        style = MaterialTheme.typography.labelSmall,
                        color = electricBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = electricBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "İncele",
                        fontSize = 11.sp,
                        color = electricBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun TelegramTabContent(
    categories: List<TelegramMediaCategory>,
    selectedCategories: Set<TelegramCategoryType>,
    totalBytes: Long,
    onToggleCategory: (TelegramCategoryType) -> Unit,
    onInspectCategory: (TelegramMediaCategory) -> Unit,
    uiState: StorageCleanUiState
) {
    if (categories.isEmpty()) {
        EmptyStateView(
            title = "Telegram Medyası Temiz",
            subtitle = "Telegram klasörlerinizde birikmiş video, görsel veya belge fazlalığı bulunamadı."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 220.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TelegramHeroCard(
                    totalBytes = totalBytes,
                    categoryCount = categories.size
                )
            }

            items(categories, key = { it.type.name }) { category ->
                val selectedFiles = uiState.getSelectedFilesForTelegramCategory(category)
                val isFullyOrPartiallySelected = selectedFiles.isNotEmpty()
                TelegramCategoryCard(
                    category = category,
                    selectedCount = selectedFiles.size,
                    isSelected = isFullyOrPartiallySelected,
                    onToggle = { onToggleCategory(category.type) },
                    onInspect = { onInspectCategory(category) }
                )
            }
        }
    }
}

@Composable
private fun TelegramCategoryInspectionDialog(
    category: TelegramMediaCategory,
    selectedFilePaths: Set<String>,
    onToggleFile: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDismiss: () -> Unit,
    onOpenFile: (File) -> Unit
) {
    val electricBlue = Color(0xFF00B0FF)
    var isGridView by remember { mutableStateOf(category.type.isVisualMedia) }
    var previewFile by remember { mutableStateOf<File?>(null) }

    if (previewFile != null) {
        InAppMediaViewerDialog(
            file = previewFile!!,
            isSelected = selectedFilePaths.contains(previewFile!!.absolutePath),
            onToggleSelection = { onToggleFile(previewFile!!.absolutePath) },
            onDismiss = { previewFile = null },
            onOpenExternally = { onOpenFile(previewFile!!) }
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
                .border(1.2.dp, electricBlue.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            color = DarkSurfaceCard.copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val selectedBytes = category.files.filter { selectedFilePaths.contains(it.absolutePath) }.sumOf { it.length() }
                        Text(
                            text = "${selectedFilePaths.size} / ${category.fileCount} seçili (${FileUtils.formatFileSize(selectedBytes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = electricBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = "Görünüm Değiştir",
                                tint = electricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Select / Deselect All Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tümünü Seç", fontSize = 12.sp, color = electricBlue)
                    }
                    OutlinedButton(
                        onClick = onDeselectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Seçimi Kaldır", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Files Grid / List
                Box(modifier = Modifier.weight(1f)) {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(category.files, key = { it.absolutePath }) { file ->
                                val isChecked = selectedFilePaths.contains(file.absolutePath)
                                WhatsAppMediaGridTile(
                                    file = file,
                                    isChecked = isChecked,
                                    onToggle = { onToggleFile(file.absolutePath) },
                                    onPreview = { previewFile = file }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(category.files, key = { it.absolutePath }) { file ->
                                val isChecked = selectedFilePaths.contains(file.absolutePath)
                                WhatsAppMediaListRow(
                                    file = file,
                                    isChecked = isChecked,
                                    onToggle = { onToggleFile(file.absolutePath) },
                                    onPreview = {
                                        val ext = file.extension.lowercase()
                                        if (ext in listOf("jpg", "jpeg", "png", "webp", "mp4", "mkv", "3gp", "mov", "webm", "opus", "mp3", "m4a", "ogg", "wav", "aac")) {
                                            previewFile = file
                                        } else {
                                            onOpenFile(file)
                                        }
                                    }
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
private fun SimilarPhotosView(
    groups: List<SimilarPhotoGroup>,
    selectedPaths: Set<String>,
    totalSavingsBytes: Long,
    onToggleFile: (String) -> Unit,
    onOpenFile: (File) -> Unit
) {
    val cyberPink = Color(0xFFE040FB)
    var previewFile by remember { mutableStateOf<File?>(null) }

    if (previewFile != null) {
        InAppMediaViewerDialog(
            file = previewFile!!,
            isSelected = selectedPaths.contains(previewFile!!.absolutePath),
            onToggleSelection = { onToggleFile(previewFile!!.absolutePath) },
            onDismiss = { previewFile = null },
            onOpenExternally = { onOpenFile(previewFile!!) }
        )
    }

    if (groups.isEmpty()) {
        EmptyStateView(
            title = "Benzer Fotoğraf Bulunmadı",
            subtitle = "Kameranızda peş peşe çekilmiş gereksiz kopya veya seri fotoğraf grubu tespit edilmedi."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 220.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OptivaGlassCard(borderColor = cyberPink.copy(alpha = 0.5f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cyberPink.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = cyberPink,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Benzer & Seri Çekim Fotoğraflar",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${groups.size} grupta ${FileUtils.formatFileSize(totalSavingsBytes)} tasarruf imkanı",
                                style = MaterialTheme.typography.bodySmall,
                                color = cyberPink,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Algısal pHash motoru seri çekilen fotoğrafları eşleştirdi. '⭐ En İyi Kare' otomatik korunur, gereksiz kopyaları tek tıkla silebilirsiniz.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            items(groups, key = { it.id }) { group ->
                SimilarPhotoGroupCard(
                    group = group,
                    selectedPaths = selectedPaths,
                    onToggleFile = onToggleFile,
                    onPreviewFile = { previewFile = it }
                )
            }
        }
    }
}

@Composable
private fun SimilarPhotoGroupCard(
    group: SimilarPhotoGroup,
    selectedPaths: Set<String>,
    onToggleFile: (String) -> Unit,
    onPreviewFile: (File) -> Unit
) {
    val cyberPink = Color(0xFFE040FB)
    val bestThumbnail by rememberThumbnailState(group.bestPhoto, targetSize = 220)

    OptivaGlassCard(
        borderColor = GlassBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = cyberPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${group.totalCount} Benzer Kare",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${group.formattedSavings} kazanç",
                    style = MaterialTheme.typography.labelSmall,
                    color = cyberPink,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Best Photo (Preserved)
                item {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, NeonEmerald, RoundedCornerShape(12.dp))
                            .clickable { onPreviewFile(group.bestPhoto) }
                    ) {
                        if (bestThumbnail != null) {
                            Image(
                                bitmap = bestThumbnail!!,
                                contentDescription = group.bestPhoto.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                            }
                        }

                        // Best shot badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonEmerald.copy(alpha = 0.9f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⭐ En İyi",
                                color = DeepObsidian,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Size badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(2.dp)
                        ) {
                            Text(
                                text = FileUtils.formatFileSize(group.bestPhoto.length()),
                                color = Color.White,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Similar photos (Candidates for deletion)
                items(group.similarPhotos, key = { it.absolutePath }) { photo ->
                    val isChecked = selectedPaths.contains(photo.absolutePath)
                    val photoThumbnail by rememberThumbnailState(photo, targetSize = 220)

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isChecked) 2.dp else 1.dp,
                                color = if (isChecked) NeonCoral else GlassBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleFile(photo.absolutePath) }
                    ) {
                        if (photoThumbnail != null) {
                            Image(
                                bitmap = photoThumbnail!!,
                                contentDescription = photo.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                            }
                        }

                        if (isChecked) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                        }

                        // Checkbox
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleFile(photo.absolutePath) },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(28.dp)
                                .padding(2.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonCoral,
                                uncheckedColor = Color.White.copy(alpha = 0.8f)
                            )
                        )

                        // Preview button
                        IconButton(
                            onClick = { onPreviewFile(photo) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Önizle",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Size badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(2.dp)
                        ) {
                            Text(
                                text = FileUtils.formatFileSize(photo.length()),
                                color = if (isChecked) NeonCoral else Color.White,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlurryPhotosView(
    blurryPhotos: List<BlurryPhotoItem>,
    selectedPaths: Set<String>,
    totalBytes: Long,
    currentThreshold: Double,
    onSelectThreshold: (Double) -> Unit,
    onToggleFile: (String) -> Unit,
    onOpenFile: (File) -> Unit
) {
    var previewFile by remember { mutableStateOf<File?>(null) }

    if (previewFile != null) {
        InAppMediaViewerDialog(
            file = previewFile!!,
            isSelected = selectedPaths.contains(previewFile!!.absolutePath),
            onToggleSelection = { onToggleFile(previewFile!!.absolutePath) },
            onDismiss = { previewFile = null },
            onOpenExternally = { onOpenFile(previewFile!!) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${blurryPhotos.size} Bulanık Fotoğraf (${FileUtils.formatFileSize(totalBytes)})",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    60.0 to "Düşük",
                    80.0 to "Normal",
                    100.0 to "Hassas"
                ).forEach { (th, label) ->
                    val isSelected = currentThreshold == th
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberAmber else DarkSurfaceVariant)
                            .clickable { onSelectThreshold(th) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) DeepObsidian else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (blurryPhotos.isEmpty()) {
            EmptyStateView(
                title = "Bulanık Fotoğraf Bulunmadı",
                subtitle = "Taramada netlik skoru düşük olan veya titreyen fotoğraf tespit edilmedi."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(blurryPhotos, key = { it.path }) { item ->
                    val isChecked = selectedPaths.contains(item.path)
                    val thumbnail by rememberThumbnailState(item.file, targetSize = 220)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .border(
                                width = if (isChecked) 2.dp else 0.8.dp,
                                color = if (isChecked) CyberAmber else GlassBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { previewFile = item.file }
                    ) {
                        if (thumbnail != null) {
                            Image(
                                bitmap = thumbnail!!,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                            }
                        }

                        if (isChecked) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                        }

                        // Checkbox
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleFile(item.path) },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(28.dp)
                                .padding(2.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyberAmber,
                                uncheckedColor = Color.White.copy(alpha = 0.8f)
                            )
                        )

                        // Blur % Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "%${item.blurPercentage} Bulanık",
                                color = CyberAmber,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bottom scrim with file size
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(4.dp)
                        ) {
                            Text(
                                text = item.formattedSize,
                                color = Color.White,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsAndApksView(
    items: List<DownloadApkItem>,
    selectedPaths: Set<String>,
    activeFilter: DownloadFilterType,
    onSelectFilter: (DownloadFilterType) -> Unit,
    onToggleItem: (String) -> Unit,
    onOpenFile: (File) -> Unit
) {
    val emeraldGreen = Color(0xFF00E676)
    val filteredItems = remember(items, activeFilter) {
        when (activeFilter) {
            DownloadFilterType.ALL -> items
            DownloadFilterType.ALREADY_INSTALLED_APKS -> items.filter { it.isAlreadyInstalled }
            DownloadFilterType.OLD_DOWNLOADS -> items.filter { it.isOld }
            DownloadFilterType.ARCHIVES -> items.filter { it.type == DownloadType.ARCHIVE }
            DownloadFilterType.DOCUMENTS -> items.filter { it.type == DownloadType.DOCUMENT }
        }
    }

    val totalBytes = remember(items) { items.sumOf { it.sizeBytes } }
    val alreadyInstalledCount = remember(items) { items.count { it.isAlreadyInstalled } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Hero summary card
        OptivaGlassCard(
            borderColor = emeraldGreen.copy(alpha = 0.5f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(emeraldGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            tint = emeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "İndirilenler & APK Analizörü",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${items.size} dosya • ${FileUtils.formatFileSize(totalBytes)} toplam alan",
                            style = MaterialTheme.typography.bodySmall,
                            color = emeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (alreadyInstalledCount > 0) {
                        "Telefonunuzda zaten kurulu olan $alreadyInstalledCount adet gereksiz APK paketi tespit edildi. Bunları silmek kurulu uygulamaları etkilemez."
                    } else {
                        "İndirilenler ve sohbet belgeleri klasörlerinizdeki unutulmuş arşiv, kurulum paketi ve belgeler listelenmektedir."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf(
                DownloadFilterType.ALL to "Tümü (${items.size})",
                DownloadFilterType.ALREADY_INSTALLED_APKS to "⭐ Kurulu APK ($alreadyInstalledCount)",
                DownloadFilterType.OLD_DOWNLOADS to "Eski İndirilenler",
                DownloadFilterType.ARCHIVES to "Arşivler (ZIP/RAR)",
                DownloadFilterType.DOCUMENTS to "Belgeler (PDF)"
            )
            items(chips) { (type, label) ->
                val isSelected = activeFilter == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) emeraldGreen else DarkSurfaceVariant)
                        .clickable { onSelectFilter(type) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) DeepObsidian else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredItems.isEmpty()) {
            EmptyStateView(
                title = "Bu Filtrede Dosya Yok",
                subtitle = "Seçili filtre kategorisinde herhangi bir dosya tespit edilmedi."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 220.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.path }) { item ->
                    val isChecked = selectedPaths.contains(item.path)
                    DownloadApkItemCard(
                        item = item,
                        isChecked = isChecked,
                        onToggle = { onToggleItem(item.path) },
                        onOpen = { onOpenFile(item.file) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadApkItemCard(
    item: DownloadApkItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    val emeraldGreen = Color(0xFF00E676)
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    OptivaGlassCard(
        borderColor = if (isChecked) emeraldGreen.copy(alpha = 0.6f) else OutlineDark,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = emeraldGreen,
                    uncheckedColor = TextMuted
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Icon box depending on type
            val (iconVector, iconColor) = when (item.type) {
                DownloadType.APK -> Icons.Default.Android to emeraldGreen
                DownloadType.ARCHIVE -> Icons.Default.Archive to CyberPurple
                DownloadType.DOCUMENT -> Icons.Default.Description to NeonCyan
                DownloadType.OTHER -> Icons.Default.Folder to TextSecondary
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.apkAppName ?: item.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Badge
                    if (item.isAlreadyInstalled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(emeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Kurulu APK",
                                color = emeraldGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (item.isApk) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Kurulmamış",
                                color = CyberAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.formattedSize} • ${dateFormat.format(Date(item.lastModified))}" +
                            if (item.apkVersionName != null) " • v${item.apkVersionName}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isAlreadyInstalled) emeraldGreen else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = item.path,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onOpen,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Aç",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
