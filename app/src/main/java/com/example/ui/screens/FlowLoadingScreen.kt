package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.R
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * MODULE 1: Top-Aligned FlowLoadingScreen
 * - Layout uses Modifier.fillMaxSize().statusBarsPadding().
 * - Top Alignment: Lottie paperplane animation (loading.json) positioned at the very top
 *   of the screen right below the status bar inside a compact glowing glass container (Modifier.size(130.dp)).
 * - Corrected FlowCBSE branding with graduation mortarboard cap & stacked books icon.
 * - Subtitle ("CBSE NCERT Multi-Student Study Suite") and fading contextual loading text.
 * - Smooth horizontal glass progress bar and floating deep-focus canvas particles.
 */
@Composable
fun FlowLoadingScreen(
    modifier: Modifier = Modifier,
    targetGrade: Int = 10,
    studentName: String? = null,
    onLoadingComplete: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    // Lottie Composition from asset "loading.json"
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("loading.json"))
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 1.05f
    )

    // Contextual fading phrases
    val phrases = remember(targetGrade, studentName) {
        listOf(
            "Initializing NCERT Vault...",
            "Waking up Gemini Study Companion...",
            "Calibrating CBSE Weakness Radar...",
            "Indexing Class $targetGrade Curriculum...",
            "Preparing personalized study flow..."
        )
    }

    var phraseIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1900)
            phraseIndex = (phraseIndex + 1) % phrases.size
        }
    }

    // Progress Bar Animation (0.0f -> 1.0f)
    var progressTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
        label = "LoadProgress"
    )

    LaunchedEffect(Unit) {
        progressTarget = 1.0f
        delay(2800)
        onLoadingComplete()
    }

    // Floating Focus Particles
    val particleCount = 24
    val random = remember { Random(42) }
    val initialParticles = remember {
        List(particleCount) {
            Particle(
                xNorm = random.nextFloat(),
                yNorm = random.nextFloat(),
                radius = random.nextFloat() * 3.5f + 1.5f,
                speed = random.nextFloat() * 0.08f + 0.04f,
                alpha = random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AmbientMotion")
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticlePhase"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .statusBarsPadding()
            .testTag("flow_loading_screen")
    ) {
        // Deep background mesh gradient blur
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x336366F1), // #6366F1
                            Color(0x22EC4899), // #EC4899
                            Color.Transparent
                        )
                    )
                )
        )

        // Floating Canvas Particles simulating deep focus
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            initialParticles.forEach { p ->
                val currentY = ((p.yNorm - (particlePhase * p.speed)) % 1f + 1f) % 1f * h
                val currentX = p.xNorm * w
                drawCircle(
                    color = Color(0xFF6366F1).copy(alpha = p.alpha * glowPulse),
                    radius = p.radius.dp.toPx(),
                    center = Offset(currentX, currentY)
                )
            }
        }

        // Top-aligned content layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. TOP-ALIGNED LOTTIE CONTAINER (compact 130.dp glass container)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xD916161A), // #16161A with 85% opacity
                                Color(0xF0181724)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = glowPulse),
                                Color(0xFFEC4899).copy(alpha = glowPulse),
                                Color(0xFF6366F1).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(10.dp)
                    .testTag("top_lottie_container"),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow inside the container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .blur(20.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.35f),
                                    Color(0xFFEC4899).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Lottie Animation (Paperplane)
                LottieAnimation(
                    composition = composition,
                    progress = { lottieProgress },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("lottie_loading_anim")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. BRANDING: Graduation Mortarboard Cap & Stacked Books Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF6366F1).copy(alpha = 0.25f),
                                Color(0xFFEC4899).copy(alpha = 0.20f)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(LiquidGlowIndigo, LiquidGlowPink, LiquidGlowCyan)
                        ),
                        CircleShape
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flow_cbse_logo),
                    contentDescription = "FlowCBSE Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Brand Title: FlowCBSE
            Text(
                text = "FlowCBSE",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "CBSE NCERT Multi-Student Study Suite",
                fontSize = 13.sp,
                color = LiquidGlowCyan,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Fading Contextual Loading Text
            AnimatedContent(
                targetState = phrases[phraseIndex],
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 3 })
                        .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutVertically { -it / 3 })
                },
                label = "SubtitlePhraseAnimation"
            ) { phrase ->
                Text(
                    text = phrase,
                    fontSize = 13.sp,
                    color = LiquidGlowCyan,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Smooth Horizontal Glass Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1E26))
                    .border(0.8.dp, Color(0x336366F1), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFFEC4899),
                                    LiquidGlowCyan
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage indicator
            Text(
                text = "${(animatedProgress * 100).toInt()}% Initialized",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Skip / Instant Enter Button
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2216161A))
                    .border(1.dp, Color(0x226366F1), RoundedCornerShape(12.dp))
                    .clickable {
                        HapticHelper.triggerImpact(haptic)
                        onLoadingComplete()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("btn_skip_loading")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Enter Study Vault",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = LiquidGlowCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private data class Particle(
    val xNorm: Float,
    val yNorm: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float
)
