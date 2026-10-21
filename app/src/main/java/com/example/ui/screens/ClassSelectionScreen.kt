package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*

/**
 * MODULE 2: ClassSelectionScreen
 * Visual Grid of Classes 1 through 12 adhering to Liquid Glass & Fluid Motion.
 * Shows active class badge, NCERT syllabus badge, and fast routing into SubjectSelectionScreen.
 */
@Composable
fun ClassSelectionScreen(
    repository: StudyFluidRepository,
    onSelectClass: (Int) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val activeProfile = repository.activeProfile
    val currentClass = activeProfile.cbseClass

    val classes = remember { (1..12).toList() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("class_selection_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = {
                            HapticHelper.triggerSelection(haptic)
                            onBack()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LiquidSurfaceElevated)
                            .border(1.dp, LiquidCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LiquidGlowCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Text(
                        text = "NCERT Curriculum Classes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Select student grade standard (Class 1 to 12)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Class Quick Banner
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowIndigo
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                                .border(1.dp, LiquidGlowIndigo, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎓", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Active Student: ${activeProfile.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Currently in CBSE Class $currentClass",
                                fontSize = 11.sp,
                                color = LiquidGlowCyan
                            )
                        }
                    }

                    Button(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            onSelectClass(currentClass)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Open Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Classes 1-12 Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(classes) { grade ->
                    val isCurrent = grade == currentClass
                    val isBoardExamYear = grade == 10 || grade == 12

                    val borderColor = when {
                        isCurrent -> LiquidGlowCyan
                        isBoardExamYear -> LiquidGlowPink
                        else -> LiquidCardBorder
                    }

                    val glowColor = when {
                        isCurrent -> LiquidGlowIndigo
                        isBoardExamYear -> LiquidGlowPink
                        else -> null
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .testTag("class_grid_item_$grade"),
                        glowAccentColor = glowColor,
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            onSelectClass(grade)
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isBoardExamYear) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LiquidGlowPink.copy(alpha = 0.25f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "BOARD", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = LiquidGlowPink)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(LiquidGlowEmerald)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Class $grade",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCurrent) LiquidGlowCyan else TextPrimary
                                )
                                Text(
                                    text = if (grade >= 11) "Senior Sec." else if (grade >= 9) "Secondary" else if (grade >= 6) "Middle" else "Primary",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            Text(
                                text = "NCERT Syllabus →",
                                fontSize = 9.sp,
                                color = if (isCurrent) LiquidGlowCyan else TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
