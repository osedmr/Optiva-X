package com.optivax.app.presentation.components

import android.media.MediaPlayer
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.optivax.app.core.FileUtils
import com.optivax.app.core.rememberFullMediaState
import com.optivax.app.presentation.theme.OptivaColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatAudioTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

/**
 * Dedicated In-App Cyberpunk Audio Player for WhatsApp Voice Notes (.opus) and Audio files (.mp3, .m4a, etc.).
 */
@Composable
fun InAppAudioPlayer(
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
                .background(OptivaColors.BrandPrimaryContainer)
                .border(2.dp, OptivaColors.BorderBrand, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isVoiceNote) Icons.Default.Mic else Icons.Default.Audiotrack,
                contentDescription = null,
                tint = OptivaColors.BrandPrimary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isVoiceNote) "WhatsApp Sesli Mesajı" else "Ses Dosyası",
            style = MaterialTheme.typography.titleMedium,
            color = OptivaColors.BrandPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Animated Waveform Equalizer
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
                        .background(if (isPlayed) OptivaColors.BrandPrimary else OptivaColors.SurfaceCard)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = OptivaColors.Danger,
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
                    thumbColor = OptivaColors.BrandPrimary,
                    activeTrackColor = OptivaColors.BrandPrimary,
                    inactiveTrackColor = OptivaColors.SurfaceCard
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
                    color = OptivaColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatAudioTime(durationMs),
                    color = OptivaColors.TextMuted,
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
                    .background(OptivaColors.SurfaceCard)
            ) {
                Text("-5s", color = OptivaColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(OptivaColors.BrandPrimary)
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
                    tint = OptivaColors.CanvasBase,
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
                    .background(OptivaColors.SurfaceInteractive)
            ) {
                Text("+5s", color = OptivaColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Dedicated In-App Cyberpunk Video Player with embedded controls.
 */
@Composable
fun InAppVideoPlayer(
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
fun InAppMediaViewerDialog(
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
            color = OptivaColors.CanvasBase
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
                                .background(OptivaColors.SurfaceCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = OptivaColors.TextPrimary,
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
                                color = OptivaColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${FileUtils.formatFileSize(file.length())} • ${dateFormat.format(Date(file.lastModified()))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OptivaColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = onOpenExternally,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OptivaColors.SurfaceCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Dışarıda Aç",
                                tint = OptivaColors.BrandPrimary,
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
                                        CircularProgressIndicator(color = OptivaColors.BrandPrimary)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Görsel Yükleniyor...", color = OptivaColors.TextSecondary)
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
                                            .background(OptivaColors.SurfaceCard),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = OptivaColors.BrandPrimary,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OptivaColors.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onOpenExternally,
                                        colors = ButtonDefaults.buttonColors(containerColor = OptivaColors.BrandPrimary, contentColor = OptivaColors.CanvasBase),
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
                                containerColor = if (isSelected) OptivaColors.Danger else OptivaColors.BrandPrimary,
                                contentColor = if (isSelected) Color.White else OptivaColors.CanvasBase
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
