package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LiquidGlowCyan
import com.example.ui.theme.LiquidGlowIndigo
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    barCount: Int = 28,
    primaryColor: Color = LiquidGlowIndigo,
    secondaryColor: Color = LiquidGlowCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveformTransition")

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val barWidth = (width / barCount) * 0.55f
        val spacing = width / barCount

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val dynamicFactor = if (isPlaying) {
                // Organic harmonic heights that bounce with playback
                val s1 = sin(progress * 4f * Math.PI.toFloat() + pulsePhase)
                val s2 = sin(progress * 8f + pulsePhase * 1.5f)
                (0.25f + 0.55f * (0.5f * (s1 + s2)).coerceIn(0f, 1f))
            } else {
                0.15f + 0.12f * sin(progress * Math.PI.toFloat())
            }

            val barHeight = (canvasHeight * dynamicFactor).coerceIn(4f, canvasHeight)
            val x = i * spacing + (spacing - barWidth) / 2
            val y = (canvasHeight - barHeight) / 2

            val brush = Brush.verticalGradient(
                colors = listOf(
                    secondaryColor,
                    primaryColor
                ),
                startY = y,
                endY = y + barHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
