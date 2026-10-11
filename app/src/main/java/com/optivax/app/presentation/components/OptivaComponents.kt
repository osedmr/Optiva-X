package com.optivax.app.presentation.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.optivax.app.domain.model.StorageBreakdown
import com.optivax.app.presentation.theme.OptivaColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium Glassmorphic Card with subtle specularity and rounded corners.
 */
@Composable
fun OptivaGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = OptivaColors.BorderSubtle,
    backgroundColor: Color = OptivaColors.SurfaceCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

/**
 * High-Tech HUD Telemetry Circular Gauge with animated ticks and glowing arc.
 */
@Composable
fun HudCircularGauge(
    percentage: Float,
    usedText: String,
    freeText: String,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    accentColor: Color? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "HudGaugeProgress"
    )

    val stateColor = accentColor ?: when {
        percentage >= 85f -> OptivaColors.Danger
        percentage >= 70f -> OptivaColors.Warning
        else -> OptivaColors.BrandPrimary
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.toPx() / 2f) - strokeWidth

            // Draw outer futuristic HUD tick marks
            val totalTicks = 40
            val startAngleRad = Math.toRadians(140.0)
            val sweepAngleRad = Math.toRadians(260.0)

            for (i in 0..totalTicks) {
                val tickAngle = startAngleRad + (sweepAngleRad * (i.toDouble() / totalTicks))
                val innerR = radius + 8.dp.toPx()
                val outerR = radius + (if (i % 5 == 0) 14.dp.toPx() else 11.dp.toPx())

                val startX = center.x + (innerR * cos(tickAngle)).toFloat()
                val startY = center.y + (innerR * sin(tickAngle)).toFloat()
                val endX = center.x + (outerR * cos(tickAngle)).toFloat()
                val endY = center.y + (outerR * sin(tickAngle)).toFloat()

                val tickColor = if (i.toFloat() / totalTicks <= animatedProgress) {
                    stateColor.copy(alpha = 0.7f)
                } else {
                    OptivaColors.SurfaceSub.copy(alpha = 0.6f)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.2.dp.toPx()
                )
            }

            // Track background arc
            drawArc(
                color = OptivaColors.SurfaceSub,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Arc
            val brush = Brush.sweepGradient(
                colors = listOf(stateColor.copy(alpha = 0.5f), stateColor, stateColor)
            )

            drawArc(
                brush = brush,
                startAngle = 140f,
                sweepAngle = 260f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center telemetry readings
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(percentage).toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                color = stateColor,
                fontWeight = FontWeight.Black,
                fontSize = 38.sp,
                letterSpacing = (-1).sp
            )

            // Status Pill Badge
            val statusColor = stateColor
            val statusText = when {
                percentage >= 85f -> "TEMİZLİK ÖNERİLİR"
                percentage >= 70f -> "DİKKAT"
                else -> "SİSTEM OPTİMİZE"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.14f))
                    .border(0.8.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$usedText Dolu",
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.TextSecondary,
                fontSize = 10.sp
            )

            Text(
                text = "Kalan: $freeText",
                style = MaterialTheme.typography.bodyMedium,
                color = OptivaColors.BrandPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            )
        }
    }
}

/**
 * Radar Scanning Wave Animation for holographic scan screens.
 */
@Composable
fun RadarScanAnimation(
    modifier: Modifier = Modifier,
    statusText: String = "Sistem Taranıyor..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                // Concentric circles
                drawCircle(color = OptivaColors.BrandPrimary.copy(alpha = 0.08f), radius = 60.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = OptivaColors.BrandPrimary.copy(alpha = 0.12f), radius = 40.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = OptivaColors.BrandPrimary.copy(alpha = 0.18f), radius = 20.dp.toPx(), style = Stroke(width = 1.dp.toPx()))

                // Rotating radar beam
                val sweepBrush = Brush.sweepGradient(
                    colors = listOf(Color.Transparent, OptivaColors.BrandPrimary.copy(alpha = 0.35f), OptivaColors.BrandPrimary)
                )

                rotate(rotation) {
                    drawArc(
                        brush = sweepBrush,
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = true
                    )
                }
            }

            // Radar pulsating center dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(OptivaColors.BrandPrimary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = statusText,
            color = OptivaColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Yapay zekalı derin analiz aktif",
            color = OptivaColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Multi-segment horizontal storage breakdown bar with responsive 2x2 legend matrix.
 */
@Composable
fun StorageProgressBar(
    breakdown: StorageBreakdown,
    modifier: Modifier = Modifier
) {
    val total = breakdown.totalBytes.toFloat().coerceAtLeast(1f)
    val appsRatio = (breakdown.appsBytes.toFloat() / total).coerceIn(0f, 1f)
    val mediaRatio = (breakdown.mediaBytes.toFloat() / total).coerceIn(0f, 1f)
    val systemRatio = (breakdown.systemBytes.toFloat() / total).coerceIn(0f, 1f)
    val otherRatio = (breakdown.otherBytes.toFloat() / total).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(OptivaColors.SurfaceSub)
        ) {
            if (appsRatio > 0) {
                Box(
                    modifier = Modifier
                        .weight(appsRatio)
                        .height(10.dp)
                        .background(OptivaColors.Info)
                )
            }
            if (mediaRatio > 0) {
                Box(
                    modifier = Modifier
                        .weight(mediaRatio)
                        .height(10.dp)
                        .background(OptivaColors.BrandPrimary)
                )
            }
            if (systemRatio > 0) {
                Box(
                    modifier = Modifier
                        .weight(systemRatio)
                        .height(10.dp)
                        .background(OptivaColors.Warning)
                )
            }
            if (otherRatio > 0) {
                Box(
                    modifier = Modifier
                        .weight(otherRatio)
                        .height(10.dp)
                        .background(OptivaColors.TextMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2x2 Responsive Category Breakdown Matrix (Zero overlapping, full width breathing space)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LegendItem(
                    label = "Uygulamalar",
                    value = breakdown.formattedApps,
                    color = OptivaColors.Info,
                    modifier = Modifier.weight(1f)
                )
                LegendItem(
                    label = "Medya & Galeri",
                    value = breakdown.formattedMedia,
                    color = OptivaColors.BrandPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LegendItem(
                    label = "Sistem & OS",
                    value = breakdown.formattedSystem,
                    color = OptivaColors.Warning,
                    modifier = Modifier.weight(1f)
                )
                LegendItem(
                    label = "Diğer & Önbellek",
                    value = breakdown.formattedOther,
                    color = OptivaColors.TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(OptivaColors.SurfaceSub)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OptivaColors.TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = OptivaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Standard primary brand button with rounded curvature and high-contrast typography.
 */
@Composable
fun OptivaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color = OptivaColors.BrandPrimary,
    contentColor: Color = OptivaColors.CanvasBase
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = OptivaColors.SurfaceElevated,
            disabledContentColor = OptivaColors.TextMuted
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = contentColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Engaging permission card with 1-tap grant button.
 */
@Composable
fun PermissionPromptCard(
    title: String,
    description: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OptivaGlassCard(
        modifier = modifier,
        borderColor = OptivaColors.Warning.copy(alpha = 0.4f),
        backgroundColor = OptivaColors.SurfaceCard
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OptivaColors.Warning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = OptivaColors.Warning,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OptivaColors.Warning,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OptivaColors.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OptivaColors.Warning, contentColor = OptivaColors.CanvasBase),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = actionText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Dialog guiding the user on configuring Developer Options animation scales to 0.5x.
 */
@Composable
fun AnimationGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = OptivaColors.BrandPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "0.5x Hızlandırma Rehberi",
                    color = OptivaColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Android cihazınızın pencereleri ve geçişleri 2 kat daha hızlı açması için standart animasyon ölçeğini 0.5x yapabilirsiniz:\n",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OptivaColors.TextSecondary
                )
                Text(
                    text = "1. 'Geliştirici Seçeneklerini Aç' butonuna dokunun.\n" +
                            "2. Aşağı kaydırarak 'Çizim' (Drawing) bölümünü bulun.\n" +
                            "3. Şu 3 ayarı '1x' yerine '0.5x' yapın:\n" +
                            "   • Pencere animasyonu ölçeği\n" +
                            "   • Geçiş animasyonu ölçeği\n" +
                            "   • Animatör süre ölçeği",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OptivaColors.TextPrimary,
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    openDeveloperSettings(context)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OptivaColors.BrandPrimary, contentColor = OptivaColors.CanvasBase)
            ) {
                Text("Geliştirici Seçeneklerini Aç", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = OptivaColors.TextSecondary)
            }
        },
        containerColor = OptivaColors.SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun openDeveloperSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(settingsIntent)
    }
}
