package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LiquidGlowCyan
import com.example.ui.theme.LiquidGlowIndigo
import com.example.ui.theme.LiquidGlowViolet
import kotlin.math.sin

@Composable
fun LiquidWaveHeader(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    primaryColor: Color = LiquidGlowIndigo,
    secondaryColor: Color = LiquidGlowCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquidWave")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val waveAmplitude = canvasHeight * 0.28f
        val baseWaterHeight = canvasHeight * 0.45f

        // First Back Wave (translucent violet/indigo)
        val pathBack = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, baseWaterHeight)
            val step = 10f
            var x = 0f
            while (x <= width) {
                val progress = x / width
                val y = baseWaterHeight + waveAmplitude * sin(progress * 2 * Math.PI.toFloat() + phase2)
                lineTo(x, y)
                x += step
            }
            lineTo(width, 0f)
            close()
        }

        drawPath(
            path = pathBack,
            brush = Brush.verticalGradient(
                colors = listOf(
                    LiquidGlowViolet.copy(alpha = 0.22f),
                    LiquidGlowIndigo.copy(alpha = 0.08f),
                    Color.Transparent
                )
            )
        )

        // Second Front Wave (vibrant cyan/indigo)
        val pathFront = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, baseWaterHeight * 0.85f)
            val step = 8f
            var x = 0f
            while (x <= width) {
                val progress = x / width
                val y = (baseWaterHeight * 0.85f) + (waveAmplitude * 0.8f) * sin(progress * 2.5f * Math.PI.toFloat() + phase1)
                lineTo(x, y)
                x += step
            }
            lineTo(width, 0f)
            close()
        }

        drawPath(
            path = pathFront,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    secondaryColor.copy(alpha = 0.15f),
                    Color.Transparent
                )
            )
        )
    }
}
