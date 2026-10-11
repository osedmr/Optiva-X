package com.optivax.app.presentation.apps

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.animateColorAsState
import com.optivax.app.core.FileUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.optivax.app.core.Constants
import com.optivax.app.core.PermissionHelper
import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.presentation.components.DeepCacheCleanerDialog
import com.optivax.app.presentation.components.OptivaGlassCard
import com.optivax.app.presentation.components.PermissionPromptCard
import androidx.compose.material.icons.filled.Bolt
import com.optivax.app.presentation.theme.OptivaColors
import com.optivax.app.presentation.theme.TextMuted
import com.optivax.app.presentation.theme.TextPrimary
import com.optivax.app.presentation.theme.TextSecondary
import java.util.Locale

@Composable
fun AppManagerScreen(
    viewModel: AppManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeepCacheCleaner by remember { mutableStateOf(false) }

    // Auto-refresh when returning from system uninstall dialog
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadApps()
    }

    if (showDeepCacheCleaner) {
        DeepCacheCleanerDialog(
            apps = uiState.apps,
            onDismiss = { showDeepCacheCleaner = false },
            onRefreshStats = { viewModel.loadApps() }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OptivaColors.CanvasBase
    ) {
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
                        text = "Uygulama Yöneticisi",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OptivaColors.TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Gerçek simgeler, bellek boyutu ve hızlı kaldırma",
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showDeepCacheCleaner = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OptivaColors.BrandPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Derin Önbellek",
                            tint = OptivaColors.BrandPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.loadApps() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OptivaColors.SurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = OptivaColors.TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                placeholder = {
                    Text("Uygulama veya paket adı ara...", color = OptivaColors.TextMuted, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = OptivaColors.BrandPrimary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle", tint = OptivaColors.TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = OptivaColors.SurfaceCard,
                    unfocusedContainerColor = OptivaColors.SurfaceCard,
                    focusedBorderColor = OptivaColors.BorderBrand,
                    unfocusedBorderColor = OptivaColors.BorderSubtle,
                    focusedTextColor = OptivaColors.TextPrimary,
                    unfocusedTextColor = OptivaColors.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Deep Cache Cleaner Banner
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                OptivaGlassCard(
                    borderColor = OptivaColors.BorderSubtle,
                    onClick = { showDeepCacheCleaner = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(OptivaColors.BrandPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Otomatik Derin Önbellek Temizleyici",
                                style = MaterialTheme.typography.titleSmall,
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Instagram, TikTok ve YouTube önbelleklerini temizleyin.",
                                style = MaterialTheme.typography.labelSmall,
                                color = OptivaColors.TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(OptivaColors.BrandPrimary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BAŞLAT",
                                color = OptivaColors.CanvasBase,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Pill Filter Tabs
            AppFilterSegmentedRow(
                currentMode = uiState.filterMode,
                userAppsCount = uiState.userAppsCount,
                idleAppsCount = uiState.idleAppsCount,
                allAppsCount = uiState.allAppsCount,
                onSelectMode = { viewModel.setFilterMode(it) }
            )

            // Idle Apps Banner if detected and not in IDLE mode
            if (uiState.idleAppsCount > 0 && uiState.filterMode != AppFilterMode.IDLE_ONLY) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    OptivaGlassCard(
                        borderColor = OptivaColors.Warning.copy(alpha = 0.5f),
                        backgroundColor = OptivaColors.SurfaceCard,
                        onClick = { viewModel.setFilterMode(AppFilterMode.IDLE_ONLY) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OptivaColors.WarningContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = OptivaColors.Warning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${uiState.idleAppsCount} Atıl Uygulama Tespit Edildi",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = OptivaColors.TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "30+ gündür açılmayan uygulamaları tek dokunuşla inceleyin.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OptivaColors.TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = "İncele →",
                                color = OptivaColors.Warning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                if (!uiState.hasUsagePermission) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        PermissionPromptCard(
                            title = "Kullanım Erişimi İzni Gerekli",
                            description = "Uygulamaların son ne zaman açıldığını ve atıl durumlarını doğru tespit etmek için kullanım erişimi iznine ihtiyaç vardır.",
                            actionText = "İzni Etkinleştir",
                            onActionClick = {
                                context.startActivity(PermissionHelper.getUsageAccessIntent(context))
                            }
                        )
                    }
                } else if (uiState.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = OptivaColors.BrandPrimary, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uygulamalar ve simgeler yükleniyor...", color = OptivaColors.TextSecondary)
                    }
                } else {
                    val displayList = uiState.filteredApps
                    if (displayList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) "Arama kriterine uygun uygulama bulunamadı." else "Bu filtreleme ölçütüne uyan uygulama yok.",
                                color = OptivaColors.TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayList, key = { it.packageName }) { app ->
                                ModernAppItemCard(
                                    app = app,
                                    isWhitelisted = uiState.whitelistedPackages.contains(app.packageName),
                                    onToggleWhitelist = {
                                        viewModel.toggleWhitelist(app.packageName, app.appName)
                                    },
                                    onOpenDetails = { openAppDetails(context, app.packageName) },
                                    onUninstall = {
                                        viewModel.handleUninstallClick(
                                            app = app,
                                            onOpenSettings = { openAppDetails(context, it) },
                                            onLaunchSystemUninstall = { requestUninstall(context, it) }
                                        )
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(140.dp)) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern Segmented Filter Row for User Apps, Idle Apps, and All Apps.
 */
@Composable
private fun AppFilterSegmentedRow(
    currentMode: AppFilterMode,
    userAppsCount: Int,
    idleAppsCount: Int,
    allAppsCount: Int,
    onSelectMode: (AppFilterMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(OptivaColors.SurfaceCard)
            .border(1.dp, OptivaColors.BorderSubtle, RoundedCornerShape(22.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val isUser = currentMode == AppFilterMode.USER_APPS
        val userBg by animateColorAsState(if (isUser) OptivaColors.BrandPrimaryContainer else Color.Transparent, label = "userBg")
        val userBorder = if (isUser) OptivaColors.BorderBrand else Color.Transparent
        val userText by animateColorAsState(if (isUser) OptivaColors.BrandPrimary else OptivaColors.TextSecondary, label = "userText")

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(19.dp))
                .background(userBg)
                .border(1.dp, userBorder, RoundedCornerShape(19.dp))
                .clickable { onSelectMode(AppFilterMode.USER_APPS) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Yüklü ($userAppsCount)",
                color = userText,
                fontWeight = if (isUser) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val isIdle = currentMode == AppFilterMode.IDLE_ONLY
        val idleBg by animateColorAsState(if (isIdle) OptivaColors.BrandPrimaryContainer else Color.Transparent, label = "idleBg")
        val idleBorder = if (isIdle) OptivaColors.BorderBrand else Color.Transparent
        val idleText by animateColorAsState(if (isIdle) OptivaColors.BrandPrimary else OptivaColors.TextSecondary, label = "idleText")

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(19.dp))
                .background(idleBg)
                .border(1.dp, idleBorder, RoundedCornerShape(19.dp))
                .clickable { onSelectMode(AppFilterMode.IDLE_ONLY) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Atıl ($idleAppsCount)",
                color = idleText,
                fontWeight = if (isIdle) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val isAll = currentMode == AppFilterMode.ALL
        val allBg by animateColorAsState(if (isAll) OptivaColors.BrandPrimaryContainer else Color.Transparent, label = "allBg")
        val allBorder = if (isAll) OptivaColors.BorderBrand else Color.Transparent
        val allText by animateColorAsState(if (isAll) OptivaColors.BrandPrimary else OptivaColors.TextSecondary, label = "allText")

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(19.dp))
                .background(allBg)
                .border(1.dp, allBorder, RoundedCornerShape(19.dp))
                .clickable { onSelectMode(AppFilterMode.ALL) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tümü ($allAppsCount)",
                color = allText,
                fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Real Crisp Android Application Icon Loader with caching.
 */
@Composable
private fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconBitmap = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(width = 96, height = 96, config = Bitmap.Config.ARGB_8888).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = null,
            modifier = modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OptivaColors.SurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                tint = OptivaColors.BrandPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun ModernAppItemCard(
    app: AppUsageInfo,
    isWhitelisted: Boolean,
    onToggleWhitelist: () -> Unit,
    onOpenDetails: () -> Unit,
    onUninstall: () -> Unit
) {
    OptivaGlassCard(
        borderColor = if (app.isIdle) OptivaColors.Warning.copy(alpha = 0.55f) else OptivaColors.BorderSubtle
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real Application Icon
                AppIcon(packageName = app.packageName)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium,
                            color = OptivaColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (app.isIdle) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OptivaColors.WarningContainer)
                                    .border(0.8.dp, OptivaColors.Warning.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Atıl (30+ gün)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OptivaColors.Warning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        } else if (app.isSystemApp) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OptivaColors.SurfaceCard)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Sistem",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OptivaColors.TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                }

                // Whitelist / Star button
                IconButton(onClick = onToggleWhitelist) {
                    Icon(
                        imageVector = if (isWhitelisted) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Muaf Tut",
                        tint = if (isWhitelisted) OptivaColors.Warning else OptivaColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Storage Details Bar (Responsive 3-Column Balanced Distribution)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.SurfaceCard)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Toplam Boyut",
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.TextSecondary,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.formattedTotalSize,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OptivaColors.BrandPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Uygulama / Veri",
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.TextSecondary,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${app.formattedAppSize} • ${app.formattedDataSize}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OptivaColors.TextPrimary,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Son Kullanım",
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.TextSecondary,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatLastUsed(app.lastTimeUsed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (app.isIdle) OptivaColors.Warning else OptivaColors.Success,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3-Segment Mini Storage Visual Bar (APK / Data / Cache)
            val totalBytes = (app.appSizeBytes + app.dataSizeBytes + app.cacheSizeBytes).coerceAtLeast(1L).toFloat()
            val apkWeight = (app.appSizeBytes.toFloat() / totalBytes).coerceAtLeast(0.02f)
            val dataWeight = (app.dataSizeBytes.toFloat() / totalBytes).coerceAtLeast(0.02f)
            val cacheWeight = (app.cacheSizeBytes.toFloat() / totalBytes).coerceAtLeast(0.02f)

            Spacer(modifier = Modifier.height(8.dp))

            // Three-segment continuous bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(OptivaColors.SurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .weight(apkWeight)
                        .fillMaxHeight()
                        .background(OptivaColors.BrandPrimary)
                )
                Spacer(modifier = Modifier.width(1.5.dp))
                Box(
                    modifier = Modifier
                        .weight(dataWeight)
                        .fillMaxHeight()
                        .background(OptivaColors.TextSecondary)
                )
                Spacer(modifier = Modifier.width(1.5.dp))
                Box(
                    modifier = Modifier
                        .weight(cacheWeight)
                        .fillMaxHeight()
                        .background(OptivaColors.Success)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini Legend for Storage Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OptivaColors.BrandPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "APK: ${app.formattedAppSize}", color = OptivaColors.TextSecondary, fontSize = 9.5.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OptivaColors.TextSecondary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Veri: ${app.formattedDataSize}", color = OptivaColors.TextSecondary, fontSize = 9.5.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OptivaColors.Success))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Önbellek: ${FileUtils.formatFileSize(app.cacheSizeBytes)}", color = OptivaColors.Success, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenDetails,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp),
                    border = BorderStroke(1.dp, OptivaColors.BorderSubtle),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = OptivaColors.TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ayarlar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OptivaColors.TextSecondary)
                }

                val isSystemOrOem = app.isSystemApp ||
                        app.packageName.startsWith("com.miui.") ||
                        app.packageName.startsWith("com.xiaomi.") ||
                        app.packageName.startsWith("com.android.") ||
                        app.packageName.startsWith("com.google.android.")

                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onUninstall,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemOrOem) OptivaColors.WarningContainer else OptivaColors.DangerContainer,
                        contentColor = if (isSystemOrOem) OptivaColors.Warning else OptivaColors.Danger
                    )
                ) {
                    Icon(
                        imageVector = if (isSystemOrOem) Icons.Default.Info else Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isSystemOrOem) OptivaColors.Warning else OptivaColors.Danger
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSystemOrOem) "Korumalı" else "Kaldır",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemOrOem) OptivaColors.Warning else OptivaColors.Danger
                    )
                }
            }
        }
    }
}

private fun formatLastUsed(lastUsedMillis: Long): String {
    if (lastUsedMillis <= 0L) return "Hiç açılmadı"
    val diff = System.currentTimeMillis() - lastUsedMillis
    val days = diff / (24L * 60L * 60L * 1000L)
    return when {
        days == 0L -> "Bugün"
        days == 1L -> "Dün"
        days < 30L -> "$days gün önce"
        days < 365L -> "${days / 30} ay önce"
        else -> "${days / 365} yıl önce"
    }
}

private fun openAppDetails(context: Context, packageName: String) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e(Constants.TAG, "[APPS] Error opening details for $packageName: ${e.message}")
    }
}

/**
 * Triggers standard Android system package uninstall prompt.
 * Requires android.permission.REQUEST_DELETE_PACKAGES in AndroidManifest.xml.
 */
private fun requestUninstall(context: Context, packageName: String) {
    Log.d(Constants.TAG, "[APPS] Requesting system uninstallation for package: $packageName")
    try {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e(Constants.TAG, "[APPS] Error launching ACTION_DELETE for $packageName: ${e.message}", e)
        // Fallback: open app details in Android Settings where user can tap Uninstall
        openAppDetails(context, packageName)
    }
}
