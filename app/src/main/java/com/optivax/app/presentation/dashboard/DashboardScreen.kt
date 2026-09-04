package com.optivax.app.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.optivax.app.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.optivax.app.core.FileUtils
import com.optivax.app.core.PermissionHelper
import com.optivax.app.presentation.components.AnimationGuideDialog
import com.optivax.app.presentation.components.DeepCacheCleanerDialog
import com.optivax.app.presentation.components.HudCircularGauge
import com.optivax.app.presentation.components.OptivaButton
import com.optivax.app.presentation.components.OptivaGlassCard
import androidx.compose.material.icons.filled.Bolt
import com.optivax.app.presentation.components.PermissionPromptCard
import com.optivax.app.presentation.components.StorageProgressBar
import com.optivax.app.presentation.theme.CyberAmber
import com.optivax.app.presentation.theme.CyberPurple
import com.optivax.app.presentation.theme.DarkSurfaceCard
import com.optivax.app.presentation.theme.DeepObsidian
import com.optivax.app.presentation.theme.GlassBorder
import com.optivax.app.presentation.theme.NeonCoral
import com.optivax.app.presentation.theme.NeonCyan
import com.optivax.app.presentation.theme.NeonEmerald
import com.optivax.app.presentation.theme.TextPrimary
import com.optivax.app.presentation.theme.TextSecondary

@Composable
fun DashboardScreen(
    onNavigateToStorageClean: () -> Unit,
    onNavigateToAppManager: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAnimationGuide by remember { mutableStateOf(false) }
    var showDeepCacheCleaner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    if (showAnimationGuide) {
        AnimationGuideDialog(onDismiss = { showAnimationGuide = false })
    }

    if (showDeepCacheCleaner) {
        DeepCacheCleanerDialog(
            apps = uiState.installedApps,
            onDismiss = { showDeepCacheCleaner = false },
            onRefreshStats = { viewModel.loadDashboard() }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Cyber Header with Optiva X logo, glowing title and refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Crisp Brand Logo with subtle glow border
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.2.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.optivax_logo),
                            contentDescription = "Optiva X Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "OPTIVA",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "X",
                                style = MaterialTheme.typography.headlineMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(0.8.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO AI",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = "Akıllı Cihaz Optimizasyon ve Hijyen Motoru",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { viewModel.refreshDashboard() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yenile",
                        tint = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Permission Prompts if missing
            if (!uiState.hasAllFilesPermission) {
                PermissionPromptCard(
                    title = "Tüm Dosyalara Erişim İzni Gerekli",
                    description = "Çift dosyaları, gizli kopyaları ve artık verileri tespit edebilmek için dosya yöneticisi erişimini açın.",
                    actionText = "Dosya İznini Aç",
                    onActionClick = {
                        val intent = PermissionHelper.getAllFilesAccessIntent(context)
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (!uiState.hasUsagePermission) {
                PermissionPromptCard(
                    title = "Kullanım İstatistiği İzni Gerekli",
                    description = "Atıl uygulamaları ve önbellek birikimlerini görebilmek için kullanım erişimini etkinleştirin.",
                    actionText = "Kullanım İznini Aç",
                    onActionClick = {
                        val intent = PermissionHelper.getUsageAccessIntent(context)
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Hero HUD Telemetry Circular Gauge Card
            OptivaGlassCard(borderColor = NeonCyan.copy(alpha = 0.28f)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val breakdown = uiState.storageBreakdown
                    HudCircularGauge(
                        percentage = breakdown?.usedPercentage ?: 0f,
                        usedText = breakdown?.formattedUsed ?: "0 B",
                        freeText = breakdown?.formattedFree ?: "0 B"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (breakdown != null) {
                        StorageProgressBar(breakdown = breakdown)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bento Hero 1-Tap Smart Clean Action Card
            OptivaGlassCard(
                borderColor = NeonEmerald.copy(alpha = 0.65f),
                backgroundColor = DarkSurfaceCard.copy(alpha = 0.95f),
                onClick = onNavigateToStorageClean
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonEmerald, NeonCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = DeepObsidian,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TEK DOKUNUŞLA OPTİMİZE ET",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Klonlar, kurulu APK'lar ve kalıntı çöpleri anında tara",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonEmerald)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "BAŞLAT",
                            color = DeepObsidian,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Freed Space Card (if any cleaned)
            if (uiState.totalFreedBytes > 0L) {
                OptivaGlassCard(borderColor = NeonEmerald.copy(alpha = 0.35f)) {
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
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Sistem Tarafından Kurtarılan Alan",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = FileUtils.formatFileSize(uiState.totalFreedBytes),
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Deep Cache Cleaner Smart Suggestion Hero Card
            OptivaGlassCard(
                borderColor = NeonEmerald.copy(alpha = 0.5f),
                onClick = { showDeepCacheCleaner = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Derin Önbellek Temizliği",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (uiState.totalCacheBytes > 0L) FileUtils.formatFileSize(uiState.totalCacheBytes) else "Erişilebilirlik",
                                    color = NeonEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Instagram, TikTok ve YouTube gibi dev uygulamaların gizli önbelleklerini otomatik temizleyin.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2x2 Cyber Telemetry Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Galeri & Klonlar
                OptivaGlassCard(
                    modifier = Modifier.weight(1f),
                    borderColor = CyberPurple.copy(alpha = 0.4f),
                    onClick = onNavigateToStorageClean
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = CyberPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Galeri & Klonlar",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Benzerler & Klonlar",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Card 2: Dosya & APK Avcısı
                OptivaGlassCard(
                    modifier = Modifier.weight(1f),
                    borderColor = NeonCoral.copy(alpha = 0.4f),
                    onClick = onNavigateToStorageClean
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = NeonCoral,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Dosya Avcısı",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Kurulu APK & Çöpler",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 3: Idle Apps
                OptivaGlassCard(
                    modifier = Modifier.weight(1f),
                    borderColor = NeonCyan.copy(alpha = 0.35f),
                    onClick = onNavigateToAppManager
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Atıl Uygulama",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${uiState.idleAppsCount} Atıl Bulundu",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.idleAppsCount > 0) CyberAmber else TextSecondary,
                            fontWeight = if (uiState.idleAppsCount > 0) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Card 4: 0.5x Speedup Guide
                OptivaGlassCard(
                    modifier = Modifier.weight(1f),
                    borderColor = NeonEmerald.copy(alpha = 0.35f),
                    onClick = { showAnimationGuide = true }
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "0.5x Motoru",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "2x Hızlandırıcı",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Bottom clearance for floating navigation bar
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}
