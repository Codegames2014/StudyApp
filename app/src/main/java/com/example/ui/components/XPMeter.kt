package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * XPMeter visually tracks progress towards the next CBSE mastery level using a
 * spring-physics animated bar with color-shifting gradients as XP increases.
 */
@Composable
fun XPMeter(
    currentXp: Int,
    pointsPerLevel: Int = 150,
    modifier: Modifier = Modifier,
    barHeight: Dp = 10.dp,
    compact: Boolean = false,
    showRankTitle: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    val currentLevel = (currentXp / pointsPerLevel) + 1
    val currentLevelBase = (currentLevel - 1) * pointsPerLevel
    val progressInLevel = (currentXp - currentLevelBase).coerceIn(0, pointsPerLevel)
    val progressFraction = (progressInLevel / pointsPerLevel.toFloat()).coerceIn(0f, 1f)
    val pointsNeeded = pointsPerLevel - progressInLevel

    val rankTitle = when {
        currentLevel >= 5 -> "CBSE Grandmaster"
        currentLevel >= 4 -> "Board Scholar"
        currentLevel >= 3 -> "Syllabus Explorer"
        currentLevel >= 2 -> "NCERT Pathfinder"
        else -> "NCERT Apprentice"
    }

    // Spring-physics progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "xpSpringProgress"
    )

    // Dynamic color-shifting gradient as XP increases across tiers
    val startGradientColor by animateColorAsState(
        targetValue = when {
            progressFraction >= 0.80f -> LiquidGlowEmerald
            progressFraction >= 0.50f -> LiquidGlowAmber
            progressFraction >= 0.25f -> Color(0xFF6366F1) // Electric Indigo
            else -> LiquidGlowCyan
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "startGradientColor"
    )

    val endGradientColor by animateColorAsState(
        targetValue = when {
            progressFraction >= 0.80f -> Color(0xFFFFD700) // Gold
            progressFraction >= 0.50f -> Color(0xFFFF6D00) // Neon Flame
            progressFraction >= 0.25f -> LiquidGlowCyan
            else -> LiquidGlowIndigo
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "endGradientColor"
    )

    // Shimmer sweep across the liquid bar
    val infiniteTransition = rememberInfiniteTransition(label = "xpShimmerTransition")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPhase"
    )

    // Pulsing tip spark glow
    val tipPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tipPulse"
    )

    // Micro-interaction bounce on tap
    var isPressed by remember { mutableStateOf(false) }
    val meterScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "meterScale"
    )

    Column(
        modifier = modifier
            .scale(meterScale)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                HapticHelper.triggerSelection(haptic)
                isPressed = true
                onClick?.invoke()
            }
            .testTag("xp_meter")
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(120)
                isPressed = false
            }
        }

        if (!compact) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(startGradientColor.copy(alpha = 0.2f))
                            .border(1.dp, startGradientColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LVL $currentLevel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = startGradientColor
                        )
                    }

                    if (showRankTitle) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rankTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // XP to next level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currentXp XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(+${pointsNeeded} to L${currentLevel + 1})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = endGradientColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Spring-physics fluid progress bar track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .background(LiquidSurfaceElevated)
                .border(0.8.dp, LiquidCardBorder, RoundedCornerShape(barHeight / 2))
        ) {
            // Filled gradient bar with spring animation and moving shimmer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackWidth = size.width
                val trackHeight = size.height
                val fillWidth = (trackWidth * animatedProgress).coerceAtLeast(0f)

                if (fillWidth > 0f) {
                    // Base color-shifting gradient
                    val baseBrush = Brush.horizontalGradient(
                        colors = listOf(startGradientColor, endGradientColor),
                        startX = 0f,
                        endX = trackWidth.coerceAtLeast(1f)
                    )

                    drawRoundRect(
                        brush = baseBrush,
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(fillWidth, trackHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                    )

                    // Liquid shimmer highlight sweep
                    val shimmerCenter = fillWidth * shimmerPhase
                    val shimmerBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        startX = (shimmerCenter - 50f).coerceAtLeast(0f),
                        endX = (shimmerCenter + 50f).coerceAtMost(fillWidth)
                    )

                    drawRoundRect(
                        brush = shimmerBrush,
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(fillWidth, trackHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                    )
                }
            }

            // Glowing indicator spark at the tip of the progress
            if (animatedProgress > 0.03f && animatedProgress < 0.99f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(barHeight * 1.3f)
                            .scale(tipPulse)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, endGradientColor, CircleShape)
                    )
                }
            }
        }

        if (!compact) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${progressInLevel} / ${pointsPerLevel} XP",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = "${(progressFraction * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = startGradientColor
                )
            }
        }
    }
}
