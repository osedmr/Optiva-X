package com.optivax.app.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.optivax.app.core.PermissionHelper
import com.optivax.app.presentation.components.AnimationGuideDialog
import com.optivax.app.presentation.components.HudCircularGauge
import com.optivax.app.presentation.components.PermissionPromptCard
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
    var showAnimationGuide by remember { mutableStateOf(false) }
    if (showAnimationGuide) {
        AnimationGuideDialog(onDismiss = { showAnimationGuide = false })
    }
    Surface(modifier = Modifier.fillMaxSize(), color = OptivaColors.CanvasBase) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Optiva X", style = MaterialTheme.typography.headlineMedium)
                    Text("Depolamanızı düzenleyin", color = OptivaColors.TextSecondary)
                }
                IconButton(onClick = viewModel::refreshDashboard, enabled = !uiState.isLoading) {
                    Icon(Icons.Default.Refresh, contentDescription = "Depolama bilgisini yenile")
                }
            }
            val storage = uiState.storageBreakdown
            if (storage != null) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Depolama", style = MaterialTheme.typography.titleMedium)
                        HudCircularGauge(storage.usedPercentage, storage.formattedUsed, storage.formattedFree)
                    }
                }
            } else {
                Text(if (uiState.isLoading) "Depolama bilgisi alınıyor…" else "Depolama bilgisi alınamadı.")
            }
            if (!uiState.hasStoragePermission) {
                PermissionPromptCard(
                    title = "Medya erişimi kapalı",
                    description = "Fotoğrafları ve medya dosyalarını incelemek için erişim izni gerekir.",
                    actionText = "İzinleri yönet",
                    onActionClick = { context.startActivity(PermissionHelper.getAppSettingsIntent(context)) }
                )
            }
            Text("Neyi incelemek istersiniz?", style = MaterialTheme.typography.titleMedium)
            DashboardDestination("Fotoğraflar", "Benzer fotoğraflar, bulanık çekimler ve ekran görüntüleri", onNavigateToGallery)
            DashboardDestination("Dosyalar", "WhatsApp, Telegram, kopya dosyalar ve indirilenler", onNavigateToFiles)
            DashboardDestination("Uygulamalar", "Kullanılmayan uygulamalar ve önbellek temizliği", onNavigateToAppManager)
            TextButton(onClick = { showAnimationGuide = true }) { Text("Yardım: animasyon ayarları") }
        }
    }
}

@Composable
private fun DashboardDestination(title: String, description: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = OptivaColors.TextSecondary)
        }
    }
}
