package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class PomodoroMode(val label: String, val defaultMinutes: Int, val accentColor: Color) {
    FOCUS_SPRINT("Focus Sprint", 25, Color(0xFF6366F1)),
    SHORT_BREAK("Short Break", 5, Color(0xFF10B981)),
    LONG_BREAK("Deep Rest", 15, Color(0xFF06B6D4))
}

/**
 * MODULE 4: PomodoroTimerCard
 * Glassmorphic interface enabling students to seamlessly toggle between work intervals (25 mins)
 * and break intervals (5 mins). Triggers distinct haptic pulses when sessions conclude.
 */
@Composable
fun PomodoroTimerCard(
    modifier: Modifier = Modifier,
    onSessionCompleted: ((PomodoroMode) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    var selectedMode by remember { mutableStateOf(PomodoroMode.FOCUS_SPRINT) }
    var totalSeconds by remember { mutableIntStateOf(selectedMode.defaultMinutes * 60) }
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var completedSessionsCount by remember { mutableIntStateOf(0) }

    fun switchMode(mode: PomodoroMode) {
        HapticHelper.triggerSelection(haptic)
        selectedMode = mode
        totalSeconds = mode.defaultMinutes * 60
        remainingSeconds = totalSeconds
        isRunning = false
    }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            // Session concluded! Trigger distinct long-press haptic pulse
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (selectedMode == PomodoroMode.FOCUS_SPRINT) {
                completedSessionsCount++
            }
            onSessionCompleted?.invoke(selectedMode)
        }
    }

    val progressFraction = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f

    val animatedFraction by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "PomodoroFraction"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pomodoro_timer_card"),
        glowAccentColor = selectedMode.accentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header & Mode Switcher Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⏱️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pomodoro Study Engine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🍅 $completedSessionsCount Sprints Done",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlowCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interval Segmented Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiquidSurfaceElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PomodoroMode.entries.forEach { mode ->
                    val isSelected = mode == selectedMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) mode.accentColor.copy(alpha = 0.35f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) mode.accentColor else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { switchMode(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mode.defaultMinutes}m ${mode.label.split(" ").first()}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Circular Countdown Canvas Ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
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

                    // Progress Arc
                    if (animatedFraction > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    selectedMode.accentColor,
                                    LiquidGlowCyan,
                                    selectedMode.accentColor
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedFraction * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isRunning) "Deep Focus Active" else "Ready to Focus",
                        fontSize = 10.sp,
                        color = if (isRunning) LiquidGlowCyan else TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Controls: Play / Pause / Reset
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        HapticHelper.triggerImpact(haptic)
                        isRunning = false
                        remainingSeconds = totalSeconds
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LiquidSurfaceElevated)
                        .border(1.dp, LiquidCardBorder, CircleShape)
                        .testTag("btn_pomodoro_reset")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Primary Button
                Button(
                    onClick = {
                        HapticHelper.triggerImpact(haptic)
                        isRunning = !isRunning
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 140.dp)
                        .testTag("btn_pomodoro_toggle"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedMode.accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause Focus" else "Start Sprint",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
