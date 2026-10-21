package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.model.CosmeticTheme
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated organic liquid mesh background gradient mimicking fluid physics.
 */
@Composable
fun AnimatedLiquidMeshBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = LiquidGlowIndigo,
    secondaryColor: Color = LiquidGlowRose,
    accentColor: Color = LiquidGlowCyan,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluidMesh")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meshPhase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meshPhase2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Blob 1: Top right orbiting primary fluid (Indigo)
            val cx1 = w * 0.7f + (w * 0.2f) * cos(phase1)
            val cy1 = h * 0.2f + (h * 0.12f) * sin(phase1)
            val r1 = w * 0.65f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.22f),
                        primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(cx1, cy1),
                    radius = r1
                ),
                radius = r1,
                center = Offset(cx1, cy1)
            )

            // Blob 2: Bottom left floating secondary fluid (Rose/Pink mesh)
            val cx2 = w * 0.3f + (w * 0.18f) * sin(phase2)
            val cy2 = h * 0.75f + (h * 0.14f) * cos(phase2)
            val r2 = w * 0.75f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.20f),
                        secondaryColor.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(cx2, cy2),
                    radius = r2
                ),
                radius = r2,
                center = Offset(cx2, cy2)
            )

            // Blob 3: Center floating cyan resonance node
            val cx3 = w * 0.5f + (w * 0.15f) * cos(phase2 * 0.8f)
            val cy3 = h * 0.45f + (h * 0.1f) * sin(phase1 * 0.7f)
            val r3 = w * 0.5f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(cx3, cy3),
                    radius = r3
                ),
                radius = r3,
                center = Offset(cx3, cy3)
            )
        }

        content()
    }
}

/**
 * Interactive fluid theme selector card with spring animation physics and haptic triggers.
 */
@Composable
fun FluidThemeSelectorCard(
    repository: StudyFluidRepository,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val themes by repository.cosmeticThemes.collectAsState()
    val activeThemeId by repository.activeThemeId.collectAsState()
    val activeTheme = themes.find { it.id == activeThemeId } ?: themes.first()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fluid_theme_selector_card"),
        glowAccentColor = Color(activeTheme.primaryColorHex)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color(activeTheme.primaryColorHex),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fluid Visual Theme Engine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(activeTheme.primaryColorHex).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = activeTheme.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(activeTheme.primaryColorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(themes, key = { it.id }) { theme ->
                    val isSelected = theme.id == activeThemeId
                    val isLocked = !theme.isUnlocked

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) LiquidSurfaceElevated else LiquidSurfaceGlass)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(theme.primaryColorHex) else LiquidCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isLocked) {
                                HapticHelper.triggerImpact(haptic)
                                repository.setActiveTheme(theme.id)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("theme_chip_${theme.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(theme.primaryColorHex),
                                                Color(theme.secondaryColorHex)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = theme.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLocked) TextMuted else TextPrimary
                                )
                                Text(
                                    text = if (isLocked) "Lvl ${theme.unlockLevel} Required" else "Active Fluid Mesh",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color(theme.primaryColorHex) else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
