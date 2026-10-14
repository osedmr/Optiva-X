package com.optivax.app.presentation.components

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.optivax.app.core.FileUtils
import com.optivax.app.core.accessibility.CacheCleanAutomationManager
import com.optivax.app.core.accessibility.CacheCleanState
import com.optivax.app.core.accessibility.CacheTargetApp
import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.presentation.theme.OptivaColors

@Composable
fun DeepCacheCleanerDialog(
    apps: List<AppUsageInfo>,
    onDismiss: () -> Unit,
    onRefreshStats: () -> Unit
) {
    val context = LocalContext.current
    var accessibilityAccepted by remember {
        mutableStateOf(com.optivax.app.core.accessibility.AccessibilityConsent.isAccepted(context))
    }
    if (!accessibilityAccepted) {
        AccessibilityDisclosureDialog(
            onAccept = {
                com.optivax.app.core.accessibility.AccessibilityConsent.accept(context)
                accessibilityAccepted = true
                if (!CacheCleanAutomationManager.isServiceConnected()) {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            },
            onDecline = onDismiss
        )
        return
    }
    val automationState by CacheCleanAutomationManager.state.collectAsState()
    val isServiceConnected by CacheCleanAutomationManager.serviceConnected.collectAsState()

    // Filter apps with non-zero cache (> 1 MB) sorted descending
    val cacheApps = remember(apps) {
        apps.filter { it.cacheSizeBytes > 1024L * 1024L && !it.isSystemApp }
            .sortedByDescending { it.cacheSizeBytes }
    }

    var selectedPackages by remember(cacheApps) {
        mutableStateOf(cacheApps.map { it.packageName }.toSet())
    }

    val selectedBytes = remember(selectedPackages, cacheApps) {
        cacheApps.filter { selectedPackages.contains(it.packageName) }.sumOf { it.cacheSizeBytes }
    }

    val totalCacheBytes = remember(cacheApps) {
        cacheApps.sumOf { it.cacheSizeBytes }
    }

    Dialog(
        onDismissRequest = {
            if (automationState !is CacheCleanState.Running) {
                CacheCleanAutomationManager.resetState()
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, OptivaColors.BorderSubtle, RoundedCornerShape(24.dp)),
            color = OptivaColors.SurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OptivaColors.BrandPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Derin Önbellek Temizleyici",
                                style = MaterialTheme.typography.titleMedium,
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Erişilebilirlik ile Otomatik Sıfırlama",
                                style = MaterialTheme.typography.labelSmall,
                                color = OptivaColors.BrandPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (automationState !is CacheCleanState.Running) {
                        IconButton(
                            onClick = {
                                CacheCleanAutomationManager.resetState()
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = OptivaColors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content according to Automation State
                when (val state = automationState) {
                    is CacheCleanState.Idle -> {
                        IdleCacheCleanView(
                            context = context,
                            cacheApps = cacheApps,
                            selectedPackages = selectedPackages,
                            selectedBytes = selectedBytes,
                            totalCacheBytes = totalCacheBytes,
                            isServiceConnected = isServiceConnected,
                            onRequestAccessibility = { accessibilityAccepted = false },
                            onTogglePackage = { pkg ->
                                selectedPackages = if (selectedPackages.contains(pkg)) {
                                    selectedPackages - pkg
                                } else {
                                    selectedPackages + pkg
                                }
                            },
                            onSelectAll = { selectedPackages = cacheApps.map { it.packageName }.toSet() },
                            onDeselectAll = { selectedPackages = emptySet() },
                            onStartClean = {
                                val targets = cacheApps
                                    .filter { selectedPackages.contains(it.packageName) }
                                    .map { CacheTargetApp(it.packageName, it.appName, it.cacheSizeBytes) }
                                CacheCleanAutomationManager.startCleaning(context, targets)
                            }
                        )
                    }

                    is CacheCleanState.Running -> {
                        RunningCacheCleanView(
                            state = state,
                            onCancel = { CacheCleanAutomationManager.cancelCleaning() }
                        )
                    }

                    is CacheCleanState.Completed -> {
                        CompletedCacheCleanView(
                            state = state,
                            onDone = {
                                CacheCleanAutomationManager.resetState()
                                onRefreshStats()
                                onDismiss()
                            }
                        )
                    }

                    is CacheCleanState.Cancelled -> {
                        CancelledCacheCleanView(
                            onClose = {
                                CacheCleanAutomationManager.resetState()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleCacheCleanView(
    context: Context,
    cacheApps: List<AppUsageInfo>,
    selectedPackages: Set<String>,
    selectedBytes: Long,
    totalCacheBytes: Long,
    isServiceConnected: Boolean,
    onRequestAccessibility: () -> Unit,
    onTogglePackage: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onStartClean: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Hero Card
        OptivaGlassCard(borderColor = OptivaColors.BorderSubtle) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Temizlenebilir Gizli Önbellek",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.TextSecondary
                        )
                        Text(
                            text = FileUtils.formatFileSize(totalCacheBytes),
                            style = MaterialTheme.typography.headlineMedium,
                            color = OptivaColors.BrandPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(OptivaColors.BrandPrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${cacheApps.size} Uygulama",
                            color = OptivaColors.BrandPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = OptivaColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kişisel oturum, sohbet veya hesap verileriniz silinmez.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Accessibility Service permission banner if not enabled
        if (!isServiceConnected) {
            OptivaGlassCard(borderColor = OptivaColors.Warning.copy(alpha = 0.5f)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚠️ Erişilebilirlik Hizmeti Gerekli",
                            style = MaterialTheme.typography.titleSmall,
                            color = OptivaColors.Warning,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Android güvenlik kuralları gereği, üçüncü parti uygulamaların önbellekleri doğrudan silinemez. Optiva X, Erişilebilirlik Servisi ile sizin yerinize bu işlemi saniyeler içinde otomatik olarak gerçekleştirir.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OptivaColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onRequestAccessibility,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = OptivaColors.Warning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Erişilebilirlik Ayarlarını Aç",
                            color = OptivaColors.Warning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Selection Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${selectedPackages.size} / ${cacheApps.size} seçili",
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Row {
                TextButton(onClick = onSelectAll) {
                    Text("Tümünü Seç", color = OptivaColors.BrandPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDeselectAll) {
                    Text("Kaldır", color = OptivaColors.TextMuted, fontSize = 11.sp)
                }
            }
        }

        // App List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cacheApps, key = { it.packageName }) { app ->
                val isChecked = selectedPackages.contains(app.packageName)
                CacheAppItemRow(
                    context = context,
                    app = app,
                    isChecked = isChecked,
                    onToggle = { onTogglePackage(app.packageName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start Clean CTA Button
        Button(
            onClick = onStartClean,
            enabled = selectedPackages.isNotEmpty() && isServiceConnected,
            colors = ButtonDefaults.buttonColors(
                containerColor = OptivaColors.BrandPrimary,
                contentColor = OptivaColors.CanvasBase,
                disabledContainerColor = OptivaColors.SurfaceElevated,
                disabledContentColor = OptivaColors.TextMuted
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isServiceConnected) {
                    "Seçilenleri Otomatik Temizle (${FileUtils.formatFileSize(selectedBytes)})"
                } else {
                    "Önce Erişilebilirlik Hizmetini Açın"
                },
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CacheAppItemRow(
    context: Context,
    app: AppUsageInfo,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    val pm = context.packageManager
    val iconDrawable = remember(app.packageName) {
        try {
            pm.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isChecked) OptivaColors.BrandPrimaryContainer.copy(alpha = 0.25f) else OptivaColors.SurfaceElevated.copy(alpha = 0.4f))
            .border(
                1.dp,
                if (isChecked) OptivaColors.BorderBrand.copy(alpha = 0.6f) else OptivaColors.BorderSubtle,
                RoundedCornerShape(14.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
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

        Spacer(modifier = Modifier.width(8.dp))

        // Real app icon
        if (iconDrawable != null) {
            val bitmap = remember(iconDrawable) { iconDrawable.toBitmap(96, 96).asImageBitmap() }
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OptivaColors.BrandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleSmall,
                color = OptivaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(OptivaColors.BrandPrimaryContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = app.formattedCacheSize,
                color = OptivaColors.BrandPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun RunningCacheCleanView(
    state: CacheCleanState.Running,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(OptivaColors.BrandPrimaryContainer)
                .border(2.dp, OptivaColors.BrandPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = OptivaColors.BrandPrimary,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Otomatik Önbellek Temizliği",
            style = MaterialTheme.typography.titleLarge,
            color = OptivaColors.TextPrimary,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = state.statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = OptivaColors.BrandPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "İlerleyiş: ${state.currentIndex} / ${state.totalCount}",
            style = MaterialTheme.typography.labelSmall,
            color = OptivaColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        val progress = if (state.totalCount > 0) state.currentIndex.toFloat() / state.totalCount else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = OptivaColors.BrandPrimary,
            trackColor = OptivaColors.SurfaceElevated,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = OptivaColors.WarningContainer,
                contentColor = OptivaColors.Warning
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = OptivaColors.Warning, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("İşlemi Durdur", color = OptivaColors.Warning, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompletedCacheCleanView(
    state: CacheCleanState.Completed,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(OptivaColors.SuccessContainer)
                .border(2.dp, OptivaColors.Success, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = OptivaColors.Success,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Temizlik Başarıyla Tamamlandı!",
            style = MaterialTheme.typography.titleLarge,
            color = OptivaColors.TextPrimary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${state.cleanedCount} uygulamanın önbelleği temizlendi.\nToplam ${FileUtils.formatFileSize(state.totalFreedBytes)} depolama alanı geri kazanıldı.",
            style = MaterialTheme.typography.bodyMedium,
            color = OptivaColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = OptivaColors.BrandPrimary,
                contentColor = OptivaColors.CanvasBase
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Text("Harika", fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CancelledCacheCleanView(
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Temizlik İşlemi Durduruldu",
            style = MaterialTheme.typography.titleMedium,
            color = OptivaColors.Warning,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "İşlem sizin isteğiniz üzerine durduruldu.",
            style = MaterialTheme.typography.bodySmall,
            color = OptivaColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(
                containerColor = OptivaColors.SurfaceElevated,
                contentColor = OptivaColors.TextPrimary
            )
        ) {
            Text("Kapat")
        }
    }
}
