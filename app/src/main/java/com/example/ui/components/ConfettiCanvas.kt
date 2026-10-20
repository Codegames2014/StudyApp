package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LiquidGlowAmber
import com.example.ui.theme.LiquidGlowCyan
import com.example.ui.theme.LiquidGlowEmerald
import com.example.ui.theme.LiquidGlowIndigo
import com.example.ui.theme.LiquidGlowRose
import kotlin.random.Random

@Composable
fun ConfettiCanvas(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val particleCount = 45
    val colors = listOf(LiquidGlowIndigo, LiquidGlowCyan, LiquidGlowEmerald, LiquidGlowAmber, LiquidGlowRose)

    val randomOffsets = remember(trigger) {
        List(particleCount) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1400, easing = LinearOutSlowInEasing)
        )
    }

    if (animProgress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            randomOffsets.forEachIndexed { i, startPos ->
                val color = colors[i % colors.size]
                val currentX = (startPos.x * canvasW) + (Random(i).nextFloat() - 0.5f) * 120f * animProgress.value
                val currentY = (startPos.y * canvasH) + (canvasH * animProgress.value * 0.85f)
                val radius = (4f + (i % 5) * 1.5f) * (1f - animProgress.value * 0.4f)
                val alpha = (1f - animProgress.value).coerceIn(0f, 1f)

                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
