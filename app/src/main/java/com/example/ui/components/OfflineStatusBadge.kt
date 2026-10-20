package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class OfflineConnectionState {
    ONLINE_CLOUD,
    DOWNLOADING,
    OFFLINE_CACHED
}

@Composable
fun OfflineStatusBadge(
    state: OfflineConnectionState,
    downloadProgress: Float? = null,
    modifier: Modifier = Modifier,
    onToggle: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    // Pulsing indicator animation
    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val badgeColor by animateColorAsState(
        targetValue = when (state) {
            OfflineConnectionState.OFFLINE_CACHED -> LiquidGlowEmerald
            OfflineConnectionState.DOWNLOADING -> LiquidGlowCyan
            OfflineConnectionState.ONLINE_CLOUD -> LiquidGlowIndigo
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "badgeColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(badgeColor.copy(alpha = 0.16f))
            .border(1.2.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .then(
                if (onToggle != null) {
                    Modifier.clickable {
                        HapticHelper.triggerSelection(haptic)
                        onToggle()
                    }
                } else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("offline_status_badge"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Morphing icon / status dot
            when (state) {
                OfflineConnectionState.DOWNLOADING -> {
                    CircularProgressIndicator(
                        progress = { downloadProgress ?: 0.5f },
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = LiquidGlowCyan
                    )
                }
                OfflineConnectionState.OFFLINE_CACHED -> {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LiquidGlowEmerald)
                    )
                }
                OfflineConnectionState.ONLINE_CLOUD -> {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LiquidGlowIndigo)
                    )
                }
            }

            // Animated morphing status text
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInVertically { height -> height / 2 }) togetherWith
                            (fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    slideOutVertically { height -> -height / 2 })
                },
                label = "statusTextMorph"
            ) { targetState ->
                val (label, icon) = when (targetState) {
                    OfflineConnectionState.OFFLINE_CACHED -> Pair(
                        "Offline Ready",
                        Icons.Default.CloudDone
                    )
                    OfflineConnectionState.DOWNLOADING -> Pair(
                        "Downloading ${((downloadProgress ?: 0f) * 100).toInt()}%",
                        Icons.Default.CloudDownload
                    )
                    OfflineConnectionState.ONLINE_CLOUD -> Pair(
                        "Online Cloud",
                        Icons.Default.CloudQueue
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }
    }
}
