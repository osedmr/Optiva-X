package com.optivax.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.optivax.app.core.notification.BannerMessage
import com.optivax.app.core.notification.BannerType
import com.optivax.app.core.notification.OptivaBannerController
import com.optivax.app.presentation.theme.OptivaColors

/**
 * Top Host Composable observing the global [OptivaBannerController] and rendering
 * animated cyber top notification banners that slide in from above and auto-dismiss.
 */
@Composable
fun OptivaTopBannerHost(
    bannerController: OptivaBannerController,
    modifier: Modifier = Modifier
) {
    val currentBanner by bannerController.currentBanner.collectAsState()

    AnimatedVisibility(
        visible = currentBanner != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        currentBanner?.let { banner ->
            OptivaTopBanner(
                banner = banner,
                onDismiss = { bannerController.dismiss() }
            )
        }
    }
}

@Composable
fun OptivaTopBanner(
    banner: BannerMessage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor: Color = when (banner.type) {
        BannerType.SUCCESS -> OptivaColors.Success
        BannerType.WARNING -> OptivaColors.Warning
        BannerType.ERROR -> OptivaColors.Danger
        BannerType.INFO -> OptivaColors.BrandPrimary
    }

    val icon: ImageVector = when (banner.type) {
        BannerType.SUCCESS -> Icons.Default.CheckCircle
        BannerType.WARNING -> Icons.Default.WarningAmber
        BannerType.ERROR -> Icons.Default.ErrorOutline
        BannerType.INFO -> Icons.Default.Info
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable { onDismiss() },
        color = OptivaColors.SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                banner.message?.let { message ->
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = OptivaColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(OptivaColors.SurfaceInteractive)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = OptivaColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
