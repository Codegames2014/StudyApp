package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val color: Color,
    val isRibbon: Boolean,
    val size: Float
)

@Composable
fun CelebrationOverlay(
    trigger: Boolean,
    title: String = "Chapter Mastered!",
    subtitle: String = "Knowledge XP Awarded ⚡",
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!trigger) return
    val haptic = LocalHapticFeedback.current

    val particleCount = 65
    val colors = listOf(LiquidGlowIndigo, LiquidGlowCyan, LiquidGlowEmerald, LiquidGlowAmber, LiquidGlowRose)

    val particles = remember(trigger) {
        List(particleCount) { i ->
            val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
            val speed = Random.nextFloat() * 700f + 300f
            ConfettiParticle(
                initialX = 0.5f,
                initialY = 0.45f,
                velocityX = (cos(angle) * speed).toFloat(),
                velocityY = (sin(angle) * speed).toFloat() - 250f, // upward impulse
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                color = colors[i % colors.size],
                isRibbon = i % 2 == 0,
                size = Random.nextFloat() * 8f + 6f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }
    var showBadge by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        HapticHelper.triggerImpact(haptic)
        showBadge = true
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2200, easing = LinearOutSlowInEasing)
        )
        delay(400)
        showBadge = false
        onDismiss?.invoke()
    }

    val badgeScale by animateFloatAsState(
        targetValue = if (showBadge) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "badgeScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("celebration_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Canvas Particle Simulation
        if (animProgress.value < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val t = animProgress.value // 0f to 1f
                val gravity = 900f * (t * t)

                particles.forEach { p ->
                    val x = (p.initialX * canvasW) + (p.velocityX * t * 0.8f)
                    val y = (p.initialY * canvasH) + (p.velocityY * t * 0.8f) + gravity
                    val alpha = (1f - (t * 1.15f)).coerceIn(0f, 1f)
                    val rotation = p.rotationSpeed * t

                    if (alpha > 0f) {
                        rotate(rotation, Offset(x, y)) {
                            if (p.isRibbon) {
                                drawRect(
                                    color = p.color.copy(alpha = alpha),
                                    topLeft = Offset(x - p.size, y - p.size / 2),
                                    size = Size(p.size * 2f, p.size)
                                )
                            } else {
                                drawCircle(
                                    color = p.color.copy(alpha = alpha),
                                    radius = p.size / 2f,
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Celebratory floating badge in the center
        AnimatedVisibility(
            visible = showBadge && animProgress.value < 0.85f,
            enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(tween(300)) + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .scale(badgeScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LiquidSurfaceElevated)
                    .border(1.5.dp, LiquidGlowAmber, RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎉", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlowAmber
                    )
                }
            }
        }
    }
}
