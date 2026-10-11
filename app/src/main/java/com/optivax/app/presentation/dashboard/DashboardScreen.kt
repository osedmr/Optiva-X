package com.optivax.app.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.optivax.app.R
import com.optivax.app.core.FileUtils
import com.optivax.app.core.PermissionHelper
import com.optivax.app.presentation.components.AnimationGuideDialog
import com.optivax.app.presentation.components.DeepCacheCleanerDialog
import com.optivax.app.presentation.components.HudCircularGauge
import com.optivax.app.presentation.components.OptivaGlassCard
import com.optivax.app.presentation.components.PermissionPromptCard
import com.optivax.app.presentation.components.StorageProgressBar
import com.optivax.app.presentation.theme.OptivaColors

@Composable
fun DashboardScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToAppManager: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeepCacheCleaner by remember { mutableStateOf(false) }
    var showAnimationGuide by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    if (showDeepCacheCleaner) {
        DeepCacheCleanerDialog(
            apps = uiState.installedApps,
            onDismiss = { showDeepCacheCleaner = false },
            onRefreshStats = { viewModel.loadDashboard() }
        )
    }

    if (showAnimationGuide) {
        AnimationGuideDialog(
            onDismiss = { showAnimationGuide = false }
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
                    // Crisp Brand Logo with subtle border
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, OptivaColors.BorderBrand, RoundedCornerShape(12.dp))
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
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "X",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OptivaColors.BrandPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OptivaColors.BrandPrimaryContainer)
                                    .border(0.8.dp, OptivaColors.BorderBrand, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO AI",
                                    color = OptivaColors.BrandPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = "Akıllı Cihaz Optimizasyon ve Hijyen Motoru",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.TextSecondary,
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
                        .background(OptivaColors.SurfaceInteractive)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yenile",
                        tint = OptivaColors.BrandPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Permission Prompts if missing
            if (!uiState.hasStoragePermission) {
                PermissionPromptCard(
                    title = "Medya ve Depolama İzni Gerekli",
                    description = "Fotoğrafları, videoları ve indirilen dosyaları analiz edebilmek için medya erişim iznini etkinleştirin.",
                    actionText = "İzni Yönet",
                    onActionClick = {
                        val intent = PermissionHelper.getAppSettingsIntent(context)
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
            OptivaGlassCard(borderColor = OptivaColors.BorderSubtle) {
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
                borderColor = OptivaColors.BorderBrand,
                backgroundColor = OptivaColors.SurfaceCard,
                onClick = onNavigateToFiles
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(OptivaColors.BrandPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = OptivaColors.CanvasBase,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TEK DOKUNUŞLA HIZLI TEMİZLİK",
                            style = MaterialTheme.typography.titleMedium,
                            color = OptivaColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Klonlar, kurulu APK'lar ve kalıntı çöpleri anında tara",
                            style = MaterialTheme.typography.bodySmall,
                            color = OptivaColors.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OptivaColors.BrandPrimary)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "BAŞLAT",
                            color = OptivaColors.CanvasBase,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Suggestion 1: Deep Cache Cleaner Hero Card
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OptivaColors.BrandPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = OptivaColors.BrandPrimary,
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
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OptivaColors.BrandPrimaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (uiState.totalCacheBytes > 0L) FileUtils.formatFileSize(uiState.totalCacheBytes) else "Erişilebilirlik",
                                    color = OptivaColors.BrandPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Instagram, TikTok ve YouTube gibi dev uygulamaların gizli önbelleklerini otomatik temizleyin.",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Suggestion 2: 0.5x Speed Optimization Engine
            OptivaGlassCard(
                borderColor = OptivaColors.BorderSubtle,
                onClick = { showAnimationGuide = true }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OptivaColors.BrandPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = OptivaColors.BrandPrimary,
                                modifier = Modifier.size(24.dp)
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
                                    text = "0.5x Hızlandırma Motoru",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = OptivaColors.TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OptivaColors.BrandPrimaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "2x Seri",
                                        color = OptivaColors.BrandPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sistem pencere ve geçiş animasyonlarını hızlandırın.",
                                style = MaterialTheme.typography.labelSmall,
                                color = OptivaColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice / Prerequisite Box (Must enable Developer Options first)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(OptivaColors.WarningContainer)
                            .border(1.dp, OptivaColors.Warning.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OptivaColors.Warning,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Önemli Not: Geliştirici Seçeneklerini Açın",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OptivaColors.Warning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Bu ayarları uygulayabilmek için telefonunuzda Geliştirici Seçenekleri'ni kendinizin önceden açması gerekir:\nAyarlar > Telefon Hakkında > 'Derleme Numarası'na (Build Number) 7 kez art arda dokunun.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OptivaColors.TextPrimary.copy(alpha = 0.95f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Steps Checklist
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(OptivaColors.SurfaceSub)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Yapılacak Adımlar (Çizim Bölümü):",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptivaColors.BrandPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "• Pencere animasyonu ölçeği  ➜  0.5x\n" +
                                    "• Geçiş animasyonu ölçeği       ➜  0.5x\n" +
                                    "• Animatör süre ölçeği              ➜  0.5x",
                            style = MaterialTheme.typography.bodySmall,
                            color = OptivaColors.TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Button to trigger dialog & settings
                    Button(
                        onClick = { showAnimationGuide = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OptivaColors.BrandPrimary,
                            contentColor = OptivaColors.CanvasBase
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hızlandırma Rehberini & Ayarları Aç",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom clearance for floating navigation bar
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}
