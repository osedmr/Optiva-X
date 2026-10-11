package com.optivax.app.presentation.gallery

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.optivax.app.core.FileUtils
import com.optivax.app.core.rememberThumbnailState
import com.optivax.app.domain.model.BlurryPhotoItem
import com.optivax.app.domain.model.ScreenshotItem
import com.optivax.app.domain.model.SimilarPhotoGroup
import com.optivax.app.presentation.components.InAppMediaViewerDialog
import com.optivax.app.presentation.components.OptivaGlassCard
import com.optivax.app.presentation.components.RadarScanAnimation
import com.optivax.app.presentation.storage.StorageViewModel
import com.optivax.app.presentation.theme.OptivaColors
import com.optivax.app.presentation.theme.TextMuted
import com.optivax.app.presentation.theme.TextPrimary
import com.optivax.app.presentation.theme.TextSecondary
import com.optivax.app.presentation.trash.TrashBinDialog
import java.io.File

enum class GalleryTab {
    SIMILAR,
    BLURRY,
    SCREENSHOTS
}

@Composable
fun GalleryScreen(
    viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(GalleryTab.SIMILAR) }
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
                        text = "${FileUtils.formatFileSize(uiState.selectedBytesToClean)} boyutundaki seçilen fotoğraflar temizlenecektir.",
                        color = OptivaColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.cleanSelected()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.useTrashBin) OptivaColors.BrandPrimary else OptivaColors.Danger,
                        contentColor = OptivaColors.CanvasBase
                    )
                ) {
                    Text(if (uiState.useTrashBin) "Çöp Kutusuna Taşı" else "Kalıcı Olarak Sil", fontWeight = FontWeight.Bold)
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
                        text = "Galeri Temizlendi!",
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "Fotoğraf albümünüzde ${FileUtils.formatFileSize(uiState.cleanedBytesResult ?: 0L)} boş alan başarıyla geri kazanıldı.",
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Medya & Galeri AI",
                                style = MaterialTheme.typography.titleLarge,
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Benzer fotoğraflar, bulanık çekimler ve eski ekran görüntüleri",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Safe Trash Bin Button
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
                            onClick = { viewModel.startScan() },
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

                // Gallery Category Tabs (Similar, Blurry, Screenshots)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GalleryTabChip(
                        title = "Benzerler",
                        count = uiState.similarPhotoGroups.size,
                        isSelected = selectedTab == GalleryTab.SIMILAR,
                        onClick = { selectedTab = GalleryTab.SIMILAR },
                        modifier = Modifier.weight(1f)
                    )
                    GalleryTabChip(
                        title = "Bulanıklar",
                        count = uiState.blurryPhotos.size,
                        isSelected = selectedTab == GalleryTab.BLURRY,
                        onClick = { selectedTab = GalleryTab.BLURRY },
                        modifier = Modifier.weight(1f)
                    )
                    GalleryTabChip(
                        title = "Ekran Görüntüsü",
                        count = uiState.screenshots.size,
                        isSelected = selectedTab == GalleryTab.SCREENSHOTS,
                        onClick = { selectedTab = GalleryTab.SCREENSHOTS },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Action Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (selectedTab) {
                        GalleryTab.SIMILAR -> {
                            OutlinedButton(
                                onClick = { viewModel.selectAllSimilarPhotosSuggested() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = OptivaColors.BrandPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("En İyiyi Koru", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (uiState.selectedSimilarPhotoPaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllSimilarPhotos() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                        GalleryTab.BLURRY -> {
                            OutlinedButton(
                                onClick = { viewModel.selectAllBlurryPhotos() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                            ) {
                                Text("Tümünü Seç", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (uiState.selectedBlurryPhotoPaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllBlurryPhotos() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                        GalleryTab.SCREENSHOTS -> {
                            OutlinedButton(
                                onClick = { viewModel.selectAllScreenshots() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OptivaColors.BrandPrimary.copy(alpha = 0.5f))
                            ) {
                                Text("Tümünü Seç", fontSize = 11.sp, color = OptivaColors.BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (uiState.selectedScreenshotPaths.isNotEmpty()) {
                                TextButton(onClick = { viewModel.deselectAllScreenshots() }) {
                                    Text("Seçimi Temizle", fontSize = 11.sp, color = OptivaColors.TextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Main Content
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.isScanning) {
                        RadarScanAnimation(
                            statusText = "Galeri ve fotoğraflar yapay zeka ile taranıyor...",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        when (selectedTab) {
                            GalleryTab.SIMILAR -> {
                                GallerySimilarPhotosView(
                                    groups = uiState.similarPhotoGroups,
                                    selectedPaths = uiState.selectedSimilarPhotoPaths,
                                    totalSavingsBytes = uiState.totalSimilarPhotosSavingsBytes,
                                    onToggleFile = { viewModel.toggleSimilarPhotoSelection(it) },
                                    onOpenFile = { FileUtils.openFileWithSystem(context, it) }
                                )
                            }
                            GalleryTab.BLURRY -> {
                                GalleryBlurryPhotosView(
                                    blurryPhotos = uiState.blurryPhotos,
                                    selectedPaths = uiState.selectedBlurryPhotoPaths,
                                    totalBytes = uiState.totalBlurryBytes,
                                    currentThreshold = uiState.blurryThreshold,
                                    onSelectThreshold = { viewModel.setBlurryThreshold(it) },
                                    onToggleFile = { viewModel.toggleBlurryPhotoSelection(it) },
                                    onOpenFile = { FileUtils.openFileWithSystem(context, it) }
                                )
                            }
                            GalleryTab.SCREENSHOTS -> {
                                GalleryScreenshotsView(
                                    screenshots = uiState.screenshots,
                                    selectedPaths = uiState.selectedScreenshotPaths,
                                    currentAgeDays = uiState.screenshotAgeThresholdDays,
                                    onSelectAgeDays = { viewModel.setScreenshotAgeThreshold(it) },
                                    onToggleFile = { viewModel.toggleScreenshotSelection(it) },
                                    onOpenFile = { FileUtils.openFileWithSystem(context, it) }
                                )
                            }
                        }
                    }
                }
            }

            // Sleek Floating Clean Recovery Bar
            val hasSelection = uiState.selectedSimilarPhotoPaths.isNotEmpty() ||
                    uiState.selectedBlurryPhotoPaths.isNotEmpty() ||
                    uiState.selectedScreenshotPaths.isNotEmpty()

            if (hasSelection && !uiState.isScanning && !uiState.isCleaning) {
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
                                        text = "Seçilen Fotoğraflar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OptivaColors.TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = FileUtils.formatFileSize(uiState.selectedBytesToClean),
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
                                    text = "Temizle",
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
private fun GalleryTabChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) OptivaColors.BrandPrimaryContainer else OptivaColors.SurfaceCard,
        animationSpec = tween(200),
        label = "TabBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
        animationSpec = tween(200),
        label = "TabBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GallerySimilarPhotosView(
    groups: List<SimilarPhotoGroup>,
    selectedPaths: Set<String>,
    totalSavingsBytes: Long,
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

    if (groups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Benzer Fotoğraf Bulunmadı",
                    style = MaterialTheme.typography.titleMedium,
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Kameranızda peş peşe çekilmiş gereksiz kopya veya seri fotoğraf grubu tespit edilmedi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OptivaColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 220.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OptivaGlassCard(borderColor = OptivaColors.BorderSubtle) {
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Benzer & Seri Çekimler",
                                style = MaterialTheme.typography.titleMedium,
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${groups.size} grupta ${FileUtils.formatFileSize(totalSavingsBytes)} tasarruf imkanı",
                                style = MaterialTheme.typography.bodySmall,
                                color = OptivaColors.BrandPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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
    val bestThumbnail by rememberThumbnailState(group.bestPhoto, targetSize = 220)

    OptivaGlassCard(
        borderColor = OptivaColors.BorderSubtle
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
                        tint = OptivaColors.BrandPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${group.totalCount} Benzer Kare",
                        style = MaterialTheme.typography.titleSmall,
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${FileUtils.formatFileSize(group.totalSavingsBytes)} tasarruf",
                    style = MaterialTheme.typography.labelSmall,
                    color = OptivaColors.BrandPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of photos in group
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (group.totalCount > 3) 220.dp else 110.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false
            ) {
                // Best Photo Tile (with star badge)
                item(key = group.bestPhoto.absolutePath) {
                    val isChecked = selectedPaths.contains(group.bestPhoto.absolutePath)
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OptivaColors.SurfaceCard)
                            .border(
                                width = if (isChecked) 2.dp else 1.5.dp,
                                color = if (isChecked) OptivaColors.Danger else OptivaColors.BrandPrimary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPreviewFile(group.bestPhoto) }
                    ) {
                        if (bestThumbnail != null) {
                            Image(
                                bitmap = bestThumbnail!!,
                                contentDescription = group.bestPhoto.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Best shot badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(OptivaColors.BrandPrimary)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = OptivaColors.CanvasBase,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "En İyi",
                                    color = OptivaColors.CanvasBase,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleFile(group.bestPhoto.absolutePath) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(2.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = OptivaColors.Danger,
                                checkmarkColor = Color.White
                            )
                        )
                    }
                }

                // Other similar photos
                items(group.similarPhotos, key = { it.absolutePath }) { file ->
                    val isChecked = selectedPaths.contains(file.absolutePath)
                    val thumbnail by rememberThumbnailState(file, targetSize = 220)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OptivaColors.SurfaceCard)
                            .border(
                                width = if (isChecked) 2.dp else 0.8.dp,
                                color = if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPreviewFile(file) }
                    ) {
                        if (thumbnail != null) {
                            Image(
                                bitmap = thumbnail!!,
                                contentDescription = file.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (isChecked) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                        }

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleFile(file.absolutePath) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(2.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = OptivaColors.BrandPrimary,
                                checkmarkColor = OptivaColors.CanvasBase
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryBlurryPhotosView(
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
                color = OptivaColors.TextPrimary,
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
                            .background(if (isSelected) OptivaColors.BrandPrimaryContainer else OptivaColors.SurfaceCard)
                            .border(1.dp, if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onSelectThreshold(th) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (blurryPhotos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OptivaColors.Success,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Bulanık Fotoğraf Bulunmadı",
                        style = MaterialTheme.typography.titleMedium,
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Taramada netlik skoru düşük olan veya titreyen fotoğraf tespit edilmedi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OptivaColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
                            .background(OptivaColors.SurfaceCard)
                            .border(
                                width = if (isChecked) 2.dp else 0.8.dp,
                                color = if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
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

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleFile(item.path) },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(28.dp)
                                .padding(2.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = OptivaColors.BrandPrimary,
                                checkmarkColor = OptivaColors.CanvasBase,
                                uncheckedColor = Color.White.copy(alpha = 0.8f)
                            )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = String.format("%.0f", item.varianceScore),
                                color = OptivaColors.Warning,
                                fontSize = 9.sp,
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
private fun GalleryScreenshotsView(
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
                        .background(if (isSelected) OptivaColors.BrandPrimaryContainer else OptivaColors.SurfaceCard)
                        .border(1.dp, if (isSelected) OptivaColors.BorderBrand else OptivaColors.BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onSelectAgeDays(days) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (screenshots.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OptivaColors.Success,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Ekran Görüntüsü Bulunmadı",
                        style = MaterialTheme.typography.titleMedium,
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seçilen zaman dilimine ait ekran görüntüsü bulunamadı.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OptivaColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val totalSize = screenshots.sumOf { it.sizeBytes }
            Text(
                text = "${screenshots.size} Ekran Görüntüsü • Toplam ${FileUtils.formatFileSize(totalSize)}",
                style = MaterialTheme.typography.labelMedium,
                color = OptivaColors.BrandPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(screenshots, key = { it.path }) { item ->
                    val isChecked = selectedPaths.contains(item.path)
                    val thumbnail by rememberThumbnailState(item.file, targetSize = 220)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OptivaColors.SurfaceCard)
                            .border(
                                width = if (isChecked) 2.dp else 0.8.dp,
                                color = if (isChecked) OptivaColors.BorderBrand else OptivaColors.BorderSubtle,
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

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleFile(item.path) },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(28.dp)
                                .padding(2.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = OptivaColors.BrandPrimary,
                                checkmarkColor = OptivaColors.CanvasBase,
                                uncheckedColor = Color.White.copy(alpha = 0.8f)
                            )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${item.ageDays}g",
                                color = OptivaColors.TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
