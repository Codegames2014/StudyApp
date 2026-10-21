package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.NcertDatabase
import com.example.data.local.StudyFluidRepository
import com.example.data.model.NcertSubject
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*

/**
 * MODULE 2: SubjectSelectionScreen
 * Core NCERT subjects with live completion percentage rings powered by Room Database StudyProgressDao.
 */
@Composable
fun SubjectSelectionScreen(
    selectedClass: Int,
    repository: StudyFluidRepository,
    onSelectSubject: (NcertSubject) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val subjects = remember(selectedClass) {
        NcertDatabase.getSubjectsForClass(selectedClass)
    }

    // Live Room Database progress flows
    val progressList by repository.getStudyProgressForActiveProfile().collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("subject_selection_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        contentDescription = "Back to Classes",
                        tint = LiquidGlowCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Class $selectedClass NCERT Subjects",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time syllabus completion tracker",
                        fontSize = 12.sp,
                        color = LiquidGlowCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subject Cards with Live Circular Completion Rings
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(subjects) { subject ->
                    val totalChapters = subject.chapters.size.coerceAtLeast(1)
                    val completedCount = subject.chapters.count { ch ->
                        progressList.any { it.chapterId == ch.id && it.isCompleted }
                    }
                    val completionRatio = (completedCount.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f)

                    SubjectMasteryCard(
                        subject = subject,
                        completedChapters = completedCount,
                        totalChapters = totalChapters,
                        completionRatio = completionRatio,
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            onSelectSubject(subject)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectMasteryCard(
    subject: NcertSubject,
    completedChapters: Int,
    totalChapters: Int,
    completionRatio: Float,
    onClick: () -> Unit
) {
    val accentColor = Color(subject.colorHex)
    val animatedRatio by animateFloatAsState(
        targetValue = completionRatio,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "SubjectCompletionRatio"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}"),
        glowAccentColor = accentColor,
        onClick = onClick
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
                // Subject Emoji Box
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = subject.iconEmoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = subject.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedChapters of $totalChapters Chapters Mastered",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NCERT Aligned",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Custom Canvas Animated Completion Percentage Ring
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
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

                    // Progress Arc with Gradient
                    if (animatedRatio > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(accentColor, LiquidGlowCyan, accentColor)
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
                    text = "${(completionRatio * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (completionRatio > 0.7f) LiquidGlowEmerald else TextPrimary
                )
            }
        }
    }
}
