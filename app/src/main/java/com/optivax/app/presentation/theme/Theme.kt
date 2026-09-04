package com.optivax.app.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OptivaCyberColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepObsidian,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = CyberPurple,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonEmerald,
    onTertiary = DeepObsidian,
    background = DeepObsidian,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = OutlineDark,
    error = NeonCoral,
    onError = TextPrimary
)

@Composable
fun OptivaXTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = OptivaCyberColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
