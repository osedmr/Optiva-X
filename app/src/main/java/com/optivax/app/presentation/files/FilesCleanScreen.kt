package com.optivax.app.presentation.files

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.optivax.app.core.FileUtils
import com.optivax.app.core.rememberThumbnailState
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.DownloadApkItem
import com.optivax.app.domain.model.DownloadType
import com.optivax.app.domain.model.DuplicateFileDetail
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.model.LargeFileItem
import com.optivax.app.domain.model.TelegramCategoryType
import com.optivax.app.domain.model.TelegramMediaCategory
import com.optivax.app.domain.model.WhatsAppCategoryType
import com.optivax.app.domain.model.WhatsAppMediaCategory
import com.optivax.app.presentation.components.InAppMediaViewerDialog
import com.optivax.app.presentation.components.OptivaGlassCard
import com.optivax.app.presentation.components.RadarScanAnimation
import com.optivax.app.presentation.storage.DownloadFilterType
import com.optivax.app.presentation.storage.StorageViewModel
import com.optivax.app.presentation.storage.StorageCleanTab
import com.optivax.app.presentation.storage.selectionFor
import com.optivax.app.presentation.theme.OptivaColors
import com.optivax.app.presentation.theme.TextMuted
import com.optivax.app.presentation.theme.TextPrimary
import com.optivax.app.presentation.theme.TextSecondary
import com.optivax.app.presentation.trash.TrashBinDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

enum class FilesCategoryTab(val title: String, val description: String) {
    WHATSAPP("WhatsApp", "Fotoğraf, video ve diğer paylaşılan dosyalar"),
    TELEGRAM("Telegram", "Fotoğraf, video ve diğer paylaşılan dosyalar"),
    DUPLICATES("Kopya dosyalar", "Aynı içeriğe sahip dosyaları inceleyin"),
    DOWNLOADS_APKS("İndirilenler ve APK", "İndirdiğiniz belgeler ve kurulum dosyaları"),
    CORPSE_FILES("Geçici dosyalar", "Önizleme önbellekleri ve geçici dosyalar"),
    LARGE_FILES("Büyük dosyalar", "Depolamada en çok yer kaplayan dosyalar")
}

@Composable
fun FilesCleanScreen(
    selectedCategoryTab: FilesCategoryTab,
    onBack: () -> Unit,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val category = StorageCleanTab.valueOf(selectedCategoryTab.name)
    val selection = uiState.selectionFor(category)
    LaunchedEffect(category) { viewModel.startScan(tab = category) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val deleteIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onDeleteIntentSenderApproved()
        } else {
            viewModel.clearPendingDeleteIntentSender()
        }
    }

    LaunchedEffect(uiState.pendingDeleteIntentSender) {
        uiState.pendingDeleteIntentSender?.let { sender ->
            val request = IntentSenderRequest.Builder(sender).build()
            deleteIntentLauncher.launch(request)
        }
    }

    // Dialogs
    if (uiState.isTrashDialogVisible) {
        TrashBinDialog(
            trashItems = uiState.trashItems,
            totalTrashBytes = uiState.totalTrashBytes,
            onRestoreItem = { viewModel.restoreTrashItem(it) },
            onPurgeItem = { viewModel.purgeTrashItem(it) },
            onRestoreAll = { viewModel.restoreAllTrash() },
            onEmptyTrash = { viewModel.emptyTrash() },
            onDismiss = { viewModel.closeTrashDialog() }
        )
    }

    // WhatsApp Inspection Dialog
    if (uiState.inspectingCategory != null) {
        val category = uiState.inspectingCategory!!
        WhatsAppCategoryInspectionDialog(
            category = category,
            selectedFilePaths = uiState.getSelectedFilesForCategory(category),
            onToggleFile = { path -> viewModel.toggleFileInWhatsAppCategory(category.type, path) },
            onSelectAll = { viewModel.selectAllFilesInWhatsAppCategory(category.type) },
            onDeselectAll = { viewModel.deselectAllFilesInWhatsAppCategory(category.type) },
            onDismiss = { viewModel.closeCategoryInspection() },
            onOpenFile = { FileUtils.openFileWithSystem(context, it) }
        )
    }

    // Telegram Inspection Dialog
    if (uiState.inspectingTelegramCategory != null) {
        val category = uiState.inspectingTelegramCategory!!
        TelegramCategoryInspectionDialog(
            category = category,
            selectedFilePaths = uiState.getSelectedFilesForTelegramCategory(category),
            onToggleFile = { path -> viewModel.toggleFileInTelegramCategory(category.type, path) },
            onSelectAll = { viewModel.selectAllFilesInTelegramCategory(category.type) },
            onDeselectAll = { viewModel.deselectAllFilesInTelegramCategory(category.type) },
            onDismiss = { viewModel.closeTelegramCategoryInspection() },
            onOpenFile = { FileUtils.openFileWithSystem(context, it) }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = if (uiState.useTrashBin && category != StorageCleanTab.CORPSE_FILES) "Güvenli Çöp Kutusuna Taşı" else "Kalıcı Olarak Temizle",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "${FileUtils.formatFileSize(selection.selectedBytesToClean)} boyutundaki seçilen dosyalar temizlenecektir.",
                        color = OptivaColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    if (category != StorageCleanTab.CORPSE_FILES) {
                    OptivaGlassCard(
                        borderColor = OptivaColors.BorderSubtle,
                        backgroundColor = OptivaColors.SurfaceSub
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "30 Günlük Güvenli Geri Dönüşüm",
                                    color = OptivaColors.TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dosyalar çöp kutusunda tutulur, dilediğiniz zaman geri yükleyebilirsiniz.",
                                    color = OptivaColors.TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = uiState.useTrashBin,
                                onCheckedChange = { viewModel.toggleUseTrashBin(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OptivaColors.CanvasBase,
                                    checkedTrackColor = OptivaColors.BrandPrimary
                                )
                            )
                        }
                    }
                    } else {
                        Text("Geçici dosyalar kalıcı silinir. Bu işlem geri alınamaz.")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.cleanSelected(category)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.useTrashBin && category != StorageCleanTab.CORPSE_FILES) OptivaColors.BrandPrimary else OptivaColors.Danger,
                        contentColor = OptivaColors.CanvasBase
                    )
                ) {
                    Text(if (uiState.useTrashBin && category != StorageCleanTab.CORPSE_FILES) "Çöp Kutusuna Taşı" else "Kalıcı Olarak Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("İptal", color = OptivaColors.TextSecondary)
                }
            },
            containerColor = OptivaColors.SurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (uiState.cleanedBytesResult != null && (uiState.cleanedBytesResult ?: 0L) > 0L) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCleanResult() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OptivaColors.Success,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Temizlik Tamamlandı!",
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = if (uiState.useTrashBin && category != StorageCleanTab.CORPSE_FILES) "Seçilen dosyalar çöp kutusuna taşındı. Alan, kalıcı silme sonrasında boşalır." else "Silme işlemi tamamlandı.",
                    color = OptivaColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCleanResult() },
                    colors = ButtonDefaults.buttonColors(containerColor = OptivaColors.Success, contentColor = OptivaColors.CanvasBase)
                ) {
                    Text("Harika!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = OptivaColors.SurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OptivaColors.CanvasBase
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
                    IconButton(onClick = onBack, enabled = !uiState.isCleaning) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Dosyalara dön")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCategoryTab.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Yalnızca bu kategoride seçtiğiniz dosyalar işleme alınır",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.openTrashDialog() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(OptivaColors.SurfaceInteractive)
                        ) {
                            if (uiState.trashItems.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = OptivaColors.Danger,
                                            contentColor = OptivaColors.CanvasBase
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
                                        tint = OptivaColors.Warning
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Geri Dönüşüm Kutusu",
                                    tint = OptivaColors.TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.startScan(tab = category) },
                            enabled = !uiState.isScanning && !uiState.isCleaning,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(OptivaColors.SurfaceInteractive)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yeniden Tara",
                                tint = OptivaColors.BrandPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Action Selection Bar for the Active Category
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (selectedCategoryTab) {
                        FilesCategoryTab.WHATSAPP -> {
                            OutlinedButton(
                                onClick = { viewModel.selectAllWhatsAppSentFolders() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = OptivaColors.BrandPrimary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gönderilenleri Seç", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (uiState.selectedWhatsAppCategories.isNotEmpty() || uiState.selectedWhatsAppFilePaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllWhatsAppCategories() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                        FilesCategoryTab.TELEGRAM -> {
                            if (uiState.selectedTelegramCategories.isNotEmpty() || uiState.selectedTelegramFilePaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllTelegramCategories() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                        FilesCategoryTab.DUPLICATES -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.autoSelectDuplicatesKeepOldest() },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                                ) {
                                    Text("En Eskiyi Sakla", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.autoSelectDuplicatesKeepNewest() },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, OptivaColors.BorderSubtle)
                                ) {
                                    Text("En Yeniyi Sakla", fontSize = 11.sp, color = OptivaColors.TextPrimary, fontWeight = FontWeight.Medium)
                                }
                            }
                            if (uiState.selectedDuplicatePaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllDuplicates() }) {
                                    Text("Kaldır", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                        FilesCategoryTab.DOWNLOADS_APKS -> {
                            OutlinedButton(
                                onClick = { viewModel.selectAllInstalledApksSuggested() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                            ) {
                                Text("⭐ Kurulu APK'ları Seç", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (uiState.selectedDownloadPaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllDownloads() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                        FilesCategoryTab.CORPSE_FILES -> {
                            OutlinedButton(
                                onClick = { viewModel.selectAllCorpseFiles() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                            ) {
                                Text("Tümünü Seç", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (uiState.selectedCorpseItems.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllCorpseFiles() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                        FilesCategoryTab.LARGE_FILES -> {
                            OutlinedButton(
                                onClick = { viewModel.selectAllLargeFiles() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                            ) {
                                Text("Tümünü Seç", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (uiState.selectedLargeFilePaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllLargeFiles() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Main Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.errorMessage != null && !uiState.isScanning) {
                        Text(uiState.errorMessage.orEmpty(), color = OptivaColors.Danger, modifier = Modifier.padding(20.dp))
                    } else if (uiState.isScanning) {
                        RadarScanAnimation(
                            statusText = "${selectedCategoryTab.title} taranıyor...",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        when (selectedCategoryTab) {
                            FilesCategoryTab.WHATSAPP -> {
                                if (uiState.whatsAppCategories.isEmpty()) {
                                    EmptyStateBox("WhatsApp Medyası Temiz", "WhatsApp klasörlerinizde gönderilen medya kopyası veya birikmiş kalıntı bulunamadı.")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                        contentPadding = PaddingValues(bottom = 220.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            WhatsAppHeroCard(
                                                totalBytes = uiState.totalWhatsAppBytes,
                                                categoryCount = uiState.whatsAppCategories.size
                                            )
                                        }
                                        items(uiState.whatsAppCategories, key = { it.type.name }) { category ->
                                            val selectedFiles = uiState.getSelectedFilesForCategory(category)
                                            WhatsAppCategoryCard(
                                                category = category,
                                                selectedCount = selectedFiles.size,
                                                isSelected = selectedFiles.isNotEmpty(),
                                                onToggle = { viewModel.toggleWhatsAppCategory(category.type) },
                                                onInspect = { viewModel.openCategoryInspection(category) }
                                            )
                                        }
                                    }
                                }
                            }
                            FilesCategoryTab.TELEGRAM -> {
                                if (uiState.telegramCategories.isEmpty()) {
                                    EmptyStateBox("Telegram Medyası Temiz", "Telegram klasörlerinizde indirilen medya veya birikmiş önbellek bulunamadı.")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                        contentPadding = PaddingValues(bottom = 220.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            TelegramHeroCard(
                                                totalBytes = uiState.totalTelegramBytes,
                                                categoryCount = uiState.telegramCategories.size
                                            )
                                        }
                                        items(uiState.telegramCategories, key = { it.type.name }) { category ->
                                            val selectedFiles = uiState.getSelectedFilesForTelegramCategory(category)
                                            TelegramCategoryCard(
                                                category = category,
                                                selectedCount = selectedFiles.size,
                                                isSelected = selectedFiles.isNotEmpty(),
                                                onToggle = { viewModel.toggleTelegramCategory(category.type) },
                                                onInspect = { viewModel.openTelegramCategoryInspection(category) }
                                            )
                                        }
                                    }
                                }
                            }
                            FilesCategoryTab.DUPLICATES -> {
                                if (uiState.duplicates.isEmpty()) {
                                    EmptyStateBox("Çift Dosya Bulunmadı", "Cihazınızda SHA-256 sağlama değeri eşleşen gereksiz kopya bulunmamaktadır.")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
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
                            FilesCategoryTab.DOWNLOADS_APKS -> {
                                if (uiState.downloadsAndApks.isEmpty()) {
                                    EmptyStateBox("İndirilenler & APK Temiz", "İndirilenler klasörünüzde unutulmuş dosya veya eski APK yükleme paketi bulunamadı.")
                                } else {
                                    DownloadsAndApksView(
                                        items = uiState.downloadsAndApks,
                                        selectedPaths = uiState.selectedDownloadPaths,
                                        activeFilter = uiState.downloadFilterType,
                                        onSelectFilter = { viewModel.setDownloadFilterType(it) },
                                        onToggleItem = { viewModel.toggleDownloadSelection(it) },
                                        onOpenFile = { FileUtils.openFileWithSystem(context, it) }
                                    )
                                }
                            }
                            FilesCategoryTab.CORPSE_FILES -> {
                                if (uiState.corpseFiles.isEmpty()) {
                                    EmptyStateBox("Kalıntı Dosya Bulunmadı", "Cihazınızda kaldırılmış uygulamalara ait artık veri yok.")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
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
                            FilesCategoryTab.LARGE_FILES -> {
                                LargeFilesView(
                                    largeFiles = uiState.largeFiles,
                                    selectedPaths = uiState.selectedLargeFilePaths,
                                    currentThresholdBytes = uiState.largeFileSizeThreshold,
                                    onSelectThreshold = { viewModel.setLargeFileSizeThreshold(it) },
                                    onToggleFile = { viewModel.toggleLargeFileSelection(it) },
                                    onOpenFile = { FileUtils.openFileWithSystem(context, it) }
                                )
                            }
                        }
                    }
                }
            }

            // Sleek Floating Clean Recovery Bar
            val hasSelection = selection.selectedBytesToClean > 0

            if (hasSelection && !uiState.isScanning && !uiState.isCleaning && uiState.errorMessage == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 82.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = OptivaColors.SurfaceElevated.copy(alpha = 0.96f),
                        border = BorderStroke(1.dp, OptivaColors.BorderBrand.copy(alpha = 0.5f)),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(OptivaColors.BrandPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CleaningServices,
                                        contentDescription = null,
                                        tint = OptivaColors.BrandPrimary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Seçilen Dosyalar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OptivaColors.TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = FileUtils.formatFileSize(selection.selectedBytesToClean),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OptivaColors.BrandPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OptivaColors.BrandPrimary,
                                    contentColor = OptivaColors.CanvasBase
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (uiState.useTrashBin && category != StorageCleanTab.CORPSE_FILES) "Çöp kutusuna taşı" else "Kalıcı sil",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
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
private fun FileCategoryChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) OptivaColors.BrandPrimaryContainer else OptivaColors.SurfaceCard,
        animationSpec = tween(200),
        label = "FileChipBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
        animationSpec = tween(200),
        label = "FileChipBorder"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun EmptyStateBox(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = OptivaColors.Success,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OptivaColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OptivaColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// WhatsApp Components
// -------------------------------------------------------------

@Composable
private fun WhatsAppHeroCard(totalBytes: Long, categoryCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = OptivaColors.SurfaceSub,
        border = BorderStroke(1.dp, OptivaColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OptivaColors.BrandPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = OptivaColors.BrandPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WhatsApp Medya Hijyeni",
                    style = MaterialTheme.typography.bodySmall,
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$categoryCount kategoride ${FileUtils.formatFileSize(totalBytes)}",
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.BrandPrimary,
                fontWeight = FontWeight.Bold
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onToggle() },
        color = if (isSelected) OptivaColors.BrandPrimaryContainer.copy(alpha = 0.25f) else OptivaColors.SurfaceCard,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = OptivaColors.BrandPrimary,
                    checkmarkColor = OptivaColors.CanvasBase,
                    uncheckedColor = OptivaColors.TextMuted
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.BrandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (category.isRecommendedClean) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(OptivaColors.SuccessContainer)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Önerilen",
                                color = OptivaColors.Success,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${category.formattedSize} • ${category.fileCount} dosya",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (selectedCount > 0 && selectedCount != category.fileCount) {
                        Text(
                            text = " ($selectedCount seçili)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.BrandPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onInspect() },
                color = OptivaColors.SurfaceInteractive,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "İncele",
                    color = OptivaColors.BrandPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

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
                .border(1.dp, OptivaColors.BorderSubtle, RoundedCornerShape(24.dp)),
            color = OptivaColors.SurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = OptivaColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedFiles.size} / ${category.fileCount} seçili (${FileUtils.formatFileSize(selectedBytes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = OptivaColors.BrandPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(OptivaColors.SurfaceCard)
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(OptivaColors.SurfaceCard)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = OptivaColors.TextPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSelectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OptivaColors.BorderBrand)
                    ) {
                        Text("Tümünü Seç", fontSize = 12.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDeselectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OptivaColors.BorderSubtle)
                    ) {
                        Text("Seçimi Kaldır", fontSize = 12.sp, color = OptivaColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                                MediaGridTile(
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
                                MediaListRow(
                                    file = file,
                                    isChecked = isChecked,
                                    onToggle = { onToggleFile(file.absolutePath) },
                                    onPreview = { previewFile = file },
                                    onOpenFile = { onOpenFile(file) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Telegram Components
// -------------------------------------------------------------

@Composable
private fun TelegramHeroCard(totalBytes: Long, categoryCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = OptivaColors.SurfaceSub,
        border = BorderStroke(1.dp, OptivaColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OptivaColors.BrandPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = OptivaColors.BrandPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Telegram Depolama Hijyeni",
                    style = MaterialTheme.typography.bodySmall,
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$categoryCount kategoride ${FileUtils.formatFileSize(totalBytes)}",
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.BrandPrimary,
                fontWeight = FontWeight.Bold
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
    val icon = when (category.type) {
        TelegramCategoryType.VIDEOS -> Icons.Default.Videocam
        TelegramCategoryType.IMAGES -> Icons.Default.Image
        TelegramCategoryType.AUDIO -> Icons.Default.Audiotrack
        TelegramCategoryType.DOCUMENTS -> Icons.Default.Description
        TelegramCategoryType.STORIES -> Icons.Default.Visibility
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onToggle() },
        color = if (isSelected) OptivaColors.BrandPrimaryContainer.copy(alpha = 0.25f) else OptivaColors.SurfaceCard,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = OptivaColors.BrandPrimary,
                    checkmarkColor = OptivaColors.CanvasBase,
                    uncheckedColor = OptivaColors.TextMuted
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.BrandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${category.formattedSize} • ${category.fileCount} dosya",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    if (selectedCount > 0 && selectedCount != category.fileCount) {
                        Text(
                            text = " ($selectedCount seçili)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.BrandPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onInspect() },
                color = OptivaColors.SurfaceInteractive,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "İncele",
                    color = OptivaColors.BrandPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
    val selectedFiles = category.files.filter { selectedFilePaths.contains(it.absolutePath) }
    val selectedBytes = selectedFiles.sumOf { it.length() }
    var isGridView by remember { mutableStateOf(category.type == TelegramCategoryType.IMAGES || category.type == TelegramCategoryType.VIDEOS) }
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
                .border(1.dp, OptivaColors.BorderSubtle, RoundedCornerShape(24.dp)),
            color = OptivaColors.SurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = OptivaColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedFiles.size} / ${category.fileCount} seçili (${FileUtils.formatFileSize(selectedBytes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = OptivaColors.BrandPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(OptivaColors.SurfaceCard)
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(OptivaColors.SurfaceCard)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = OptivaColors.TextPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSelectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OptivaColors.BorderBrand)
                    ) {
                        Text("Tümünü Seç", fontSize = 12.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDeselectAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OptivaColors.BorderSubtle)
                    ) {
                        Text("Seçimi Kaldır", fontSize = 12.sp, color = OptivaColors.TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                                MediaGridTile(
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
                                MediaListRow(
                                    file = file,
                                    isChecked = isChecked,
                                    onToggle = { onToggleFile(file.absolutePath) },
                                    onPreview = { previewFile = file },
                                    onOpenFile = { onOpenFile(file) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Duplicates Components (SHA-256)
// -------------------------------------------------------------

@Composable
private fun DuplicateGroupCard(
    group: DuplicateItem,
    selectedPaths: Set<String>,
    onToggle: (String) -> Unit
) {
    OptivaGlassCard(borderColor = OptivaColors.BorderSubtle) {
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
                            .background(OptivaColors.BrandPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = OptivaColors.BrandPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${group.duplicates.size} Kopya Eşleşmesi",
                            style = MaterialTheme.typography.titleMedium,
                            color = OptivaColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dosya Boyutu: ${group.formattedSize}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OptivaColors.TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OptivaColors.WarningContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "İsraf: ${group.formattedWastedSize}",
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.Warning,
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
            .background(OptivaColors.SurfaceCard)
            .border(1.dp, if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onCheckedChange() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = OptivaColors.BrandPrimary,
                checkmarkColor = OptivaColors.CanvasBase,
                uncheckedColor = OptivaColors.TextMuted
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = detail.fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = OptivaColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail.filePath,
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// Downloads & APKs Components
// -------------------------------------------------------------

@Composable
private fun DownloadsAndApksView(
    items: List<DownloadApkItem>,
    selectedPaths: Set<String>,
    activeFilter: DownloadFilterType,
    onSelectFilter: (DownloadFilterType) -> Unit,
    onToggleItem: (String) -> Unit,
    onOpenFile: (File) -> Unit
) {
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
        OptivaGlassCard(borderColor = OptivaColors.BorderSubtle) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OptivaColors.BrandPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            tint = OptivaColors.BrandPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "İndirilenler & APK Analizörü",
                            style = MaterialTheme.typography.titleMedium,
                            color = OptivaColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${items.size} dosya • ${FileUtils.formatFileSize(totalBytes)} toplam alan",
                            style = MaterialTheme.typography.bodySmall,
                            color = OptivaColors.BrandPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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
                DownloadFilterType.ARCHIVES to "Arşivler",
                DownloadFilterType.DOCUMENTS to "Belgeler"
            )
            items(chips) { (type, label) ->
                val isSelected = activeFilter == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) OptivaColors.BrandPrimaryContainer else OptivaColors.SurfaceCard)
                        .border(1.dp, if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onSelectFilter(type) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 220.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun DownloadApkItemCard(
    item: DownloadApkItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    OptivaGlassCard(
        borderColor = if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
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
                    checkedColor = OptivaColors.BrandPrimary,
                    checkmarkColor = OptivaColors.CanvasBase,
                    uncheckedColor = OptivaColors.TextMuted
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            val (iconVector, iconColor) = when (item.type) {
                DownloadType.APK -> Icons.Default.Android to OptivaColors.BrandPrimary
                DownloadType.ARCHIVE -> Icons.Default.Archive to OptivaColors.BrandPrimary
                DownloadType.DOCUMENT -> Icons.Default.Description to OptivaColors.BrandPrimary
                DownloadType.OTHER -> Icons.Default.Folder to OptivaColors.TextSecondary
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.BrandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
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
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.isAlreadyInstalled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(OptivaColors.SuccessContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Kurulu APK",
                                color = OptivaColors.Success,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.formattedSize} • ${item.path}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OptivaColors.TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpen,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Aç",
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Corpse & Large Files Components
// -------------------------------------------------------------

@Composable
private fun CorpseItemCard(
    item: CorpseItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    OptivaGlassCard(
        borderColor = if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = OptivaColors.BrandPrimary,
                    checkmarkColor = OptivaColors.CanvasBase,
                    uncheckedColor = OptivaColors.TextMuted
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.BrandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.packageName,
                    style = MaterialTheme.typography.titleMedium,
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.fileCount} dosya • ${item.formattedSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OptivaColors.TextSecondary
                )
            }
        }
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
                        .background(if (isSelected) OptivaColors.BrandPrimaryContainer else OptivaColors.SurfaceCard)
                        .border(1.dp, if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onSelectThreshold(bytes) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (largeFiles.isEmpty()) {
            EmptyStateBox("Büyük Dosya Bulunmadı", "Seçili boyut kriterine (${FileUtils.formatFileSize(currentThresholdBytes)}+) uyan dosya yok.")
        } else {
            val totalSize = largeFiles.sumOf { it.sizeBytes }
            Text(
                text = "${largeFiles.size} Büyük Dosya • Toplam ${FileUtils.formatFileSize(totalSize)}",
                style = MaterialTheme.typography.labelMedium,
                color = OptivaColors.BrandPrimary,
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
        borderColor = if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
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
                    checkedColor = OptivaColors.BrandPrimary,
                    checkmarkColor = OptivaColors.CanvasBase,
                    uncheckedColor = OptivaColors.TextMuted
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.SurfaceElevated)
                    .clickable { onPreview() },
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (item.file.extension.lowercase() in listOf("mp4", "mkv", "3gp", "mov", "webm")) Icons.Default.Videocam else Icons.Default.Folder,
                        contentDescription = null,
                        tint = OptivaColors.BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.formattedSize} • ${item.path}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OptivaColors.TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Shared Tile and Row Helpers
// -------------------------------------------------------------

@Composable
private fun MediaGridTile(
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
            .background(if (isAudio) OptivaColors.SurfaceElevated else OptivaColors.SurfaceCard)
            .border(
                width = if (isChecked) 2.dp else 0.5.dp,
                color = if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
    ) {
        if (isAudio) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(OptivaColors.BrandPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVoiceNote) Icons.Default.Mic else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = OptivaColors.BrandPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isVoiceNote) "Sesli Mesaj" else ".${ext.uppercase()}",
                    color = OptivaColors.BrandPrimary,
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Image,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (isChecked) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
        }

        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.align(Alignment.TopStart).size(26.dp).padding(2.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = OptivaColors.BrandPrimary,
                checkmarkColor = OptivaColors.CanvasBase,
                uncheckedColor = Color.White.copy(alpha = 0.8f)
            )
        )

        IconButton(
            onClick = onPreview,
            modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).padding(2.dp)
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun MediaListRow(
    file: File,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit,
    onOpenFile: () -> Unit
) {
    val ext = file.extension.lowercase()
    val isMedia = ext in listOf("jpg", "jpeg", "png", "webp", "mp4", "mkv", "3gp", "mov", "webm", "opus", "mp3", "m4a", "ogg", "wav", "aac")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OptivaColors.SurfaceCard)
            .border(1.dp, if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = OptivaColors.BrandPrimary,
                checkmarkColor = OptivaColors.CanvasBase,
                uncheckedColor = OptivaColors.TextMuted
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                color = OptivaColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = FileUtils.formatFileSize(file.length()),
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.TextSecondary,
                fontSize = 11.sp
            )
        }
        IconButton(
            onClick = { if (isMedia) onPreview() else onOpenFile() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = OptivaColors.BrandPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
