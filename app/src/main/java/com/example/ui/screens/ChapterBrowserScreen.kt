package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.model.NcertChapter
import com.example.data.model.NcertSubject
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * MODULE 2: ChapterBrowserScreen
 * LazyColumn listing NCERT chapters with estimated times, key laws count, and study toggles
 * persisting directly into Room Database (StudyProgressEntity).
 */
@Composable
fun ChapterBrowserScreen(
    subject: NcertSubject,
    repository: StudyFluidRepository,
    onNavigateToChapterDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val progressList by repository.getStudyProgressForSubject(subject.id).collectAsState(initial = emptyList())

    val totalChapters = subject.chapters.size
    val completedCount = subject.chapters.count { ch ->
        progressList.any { it.chapterId == ch.id && it.isCompleted }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("chapter_browser_screen")
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
                        contentDescription = "Back to Subjects",
                        tint = LiquidGlowCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = subject.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$completedCount of $totalChapters Chapters Completed",
                        fontSize = 12.sp,
                        color = LiquidGlowCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subject Header Banner
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = Color(subject.colorHex)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = subject.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Class ${subject.cbseClass} Official NCERT Syllabus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(subject.colorHex).copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${((completedCount.toFloat() / totalChapters.coerceAtLeast(1)) * 100).toInt()}% Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(subject.colorHex)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (completedCount.toFloat() / totalChapters.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(subject.colorHex),
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chapter LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subject.chapters) { chapter ->
                    val progress = progressList.find { it.chapterId == chapter.id }
                    val isDone = progress?.isCompleted == true
                    val revisionCount = progress?.revisionCount ?: 0

                    ChapterItemRow(
                        chapter = chapter,
                        isDone = isDone,
                        revisionCount = revisionCount,
                        onToggleDone = { done ->
                            coroutineScope.launch {
                                repository.toggleChapterCompleted(subject.id, chapter.id, done)
                            }
                        },
                        onIncrementRevision = {
                            coroutineScope.launch {
                                repository.incrementChapterRevision(chapter.id)
                            }
                        },
                        onOpenDetail = {
                            HapticHelper.triggerImpact(haptic)
                            onNavigateToChapterDetail(chapter.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterItemRow(
    chapter: NcertChapter,
    isDone: Boolean,
    revisionCount: Int,
    onToggleDone: (Boolean) -> Unit,
    onIncrementRevision: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chapter_item_${chapter.id}"),
        glowAccentColor = if (isDone) LiquidGlowEmerald else LiquidGlowIndigo
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDone) LiquidGlowEmerald.copy(alpha = 0.2f) else LiquidSurfaceElevated)
                            .border(1.dp, if (isDone) LiquidGlowEmerald else LiquidCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chapter.chapterNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDone) LiquidGlowEmerald else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = chapter.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ ${chapter.estimatedReadMinutes} mins",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📐 ${chapter.keyFormulasAndLaws.size} Formulas",
                                fontSize = 10.sp,
                                color = LiquidGlowCyan
                            )
                        }
                    }
                }

                // Interactive Completed Toggle Switch
                IconButton(
                    onClick = {
                        HapticHelper.triggerImpact(haptic)
                        onToggleDone(!isDone)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDone) LiquidGlowEmerald.copy(alpha = 0.25f) else LiquidSurfaceElevated)
                        .testTag("toggle_chapter_${chapter.id}")
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (isDone) LiquidGlowEmerald else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Open NotebookLM Studio + Revision Counter Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LiquidSurfaceElevated)
                            .border(1.dp, LiquidCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🔄 $revisionCount Revisions",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            HapticHelper.triggerSelection(haptic)
                            onIncrementRevision()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add Revision",
                            tint = LiquidGlowCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Button(
                    onClick = onOpenDetail,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo.copy(alpha = 0.6f))
                ) {
                    Text(text = "NotebookLM Studio →", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
