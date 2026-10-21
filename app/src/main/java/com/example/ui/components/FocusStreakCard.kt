package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * MODULE 3: FocusStreakCard
 * Tracks consecutive days of studying with streak counter metrics and flame icons.
 * Features a custom spring-animated circular progress ring highlighting progress toward daily study goal.
 */
@Composable
fun FocusStreakCard(
    streakDays: Int,
    modifier: Modifier = Modifier,
    dailyGoalMinutes: Int = 60,
    minutesStudiedToday: Int = 45,
    knowledgePoints: Int = 640,
    onClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val progressRatio = (minutesStudiedToday.toFloat() / dailyGoalMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val animatedRatio by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "StreakGoalRatio"
    )

    // Breathing flame pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "FlameGlow")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameScale"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_streak_card"),
        glowAccentColor = LiquidGlowAmber,
        onClick = {
            HapticHelper.triggerImpact(haptic)
            onClick?.invoke()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Flame Badge with Ambient Glow
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x66F59E0B),
                                    Color(0x33F43F5E),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(1.5.dp, LiquidGlowAmber.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔥", fontSize = (24 * flameScale).sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$streakDays-Day Study Streak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Daily Goal: $minutesStudiedToday / $dailyGoalMinutes Mins Focus",
                        fontSize = 11.sp,
                        color = LiquidGlowCyan
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LiquidGlowAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ Distinction Habit",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowAmber
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "💎 $knowledgePoints XP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Spring-Animated Circular Progress Ring
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.5.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Track Ring
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress Arc with Amber to Rose Flame Gradient
                    if (animatedRatio > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFF59E0B),
                                    Color(0xFFEC4899),
                                    Color(0xFF6366F1),
                                    Color(0xFFF59E0B)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedRatio * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Text(
                    text = "${(progressRatio * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LiquidGlowAmber
                )
            }
        }
    }
}
