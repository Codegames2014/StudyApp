package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.model.RevisionTask
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.ConfettiCanvas
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun PlannerScreen(
    repository: StudyFluidRepository,
    onNavigateToChapter: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val activeProfile = repository.activeProfile
    val tasks by repository.revisionTasks.collectAsState()
    val exams by repository.examCountdowns.collectAsState()

    var selectedFilterDay by remember { mutableStateOf<Int?>(null) } // null = All, 0 = Today, 1 = Tomorrow, 3 = Day 3+
    var showConfetti by remember { mutableStateOf(false) }
    var showAiGeneratorDialog by remember { mutableStateOf(false) }

    val filteredTasks = tasks.filter {
        if (selectedFilterDay == null) true
        else if (selectedFilterDay == 3) it.dueDateOffsetDays >= 3
        else it.dueDateOffsetDays == selectedFilterDay
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Profile Link
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Revision Planner",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Spaced repetition (SuperMemo SM-2) for ${activeProfile.name}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidGlowIndigo.copy(alpha = 0.2f))
                            .border(1.dp, LiquidGlowIndigo, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SM-2 Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowIndigo
                        )
                    }
                }
            }

            // Exam Countdown Banners
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(exams.size) { idx ->
                        val exam = exams[idx]
                        GlassCard(
                            modifier = Modifier.width(280.dp),
                            glowAccentColor = LiquidGlowRose
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LiquidGlowRose.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = exam.subject, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LiquidGlowRose)
                                    }

                                    Text(
                                        text = "Target: ${exam.targetScorePercentage}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LiquidGlowCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = exam.examName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = LiquidGlowAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${exam.daysRemaining} Days Remaining",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidGlowAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Spaced Repetition Auto-Generator Action
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowAccentColor = LiquidGlowCyan,
                    onClick = {
                        HapticHelper.triggerImpact(haptic)
                        showAiGeneratorDialog = true
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LiquidGlowCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LiquidGlowCyan, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Generate Custom Study Plan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Creates calibrated SM-2 intervals based on your exam date & confidence",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Generate",
                            tint = LiquidGlowCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Timeline Spaced Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "All Tasks" to null,
                        "Due Today" to 0,
                        "Tomorrow" to 1,
                        "Day 3-7 Spaced" to 3
                    ).forEach { (label, dayOffset) ->
                        val isSelected = selectedFilterDay == dayOffset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    selectedFilterDay = dayOffset
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // Draggable Tasks List
            itemsIndexed(filteredTasks, key = { _, task -> task.id }) { index, task ->
                DraggableTaskCard(
                    task = task,
                    index = index,
                    totalCount = filteredTasks.size,
                    onToggle = {
                        HapticHelper.triggerImpact(haptic)
                        repository.toggleTaskCompletion(task.id)
                        if (!task.isCompleted) {
                            showConfetti = true
                        }
                    },
                    onMoveUp = {
                        if (index > 0) {
                            HapticHelper.triggerSelection(haptic)
                            repository.reorderTasks(index, index - 1)
                        }
                    },
                    onMoveDown = {
                        if (index < filteredTasks.size - 1) {
                            HapticHelper.triggerSelection(haptic)
                            repository.reorderTasks(index, index + 1)
                        }
                    },
                    onPostpone = {
                        HapticHelper.triggerSelection(haptic)
                        repository.moveTaskToDay(task.id, task.dueDateOffsetDays + 1)
                    },
                    onOpenChapter = { onNavigateToChapter(task.chapterId) }
                )
            }
        }

        CelebrationOverlay(
            trigger = showConfetti,
            title = "Daily Revision Complete! 🎯",
            subtitle = "Spaced Repetition Goal Reached ⚡",
            onDismiss = { showConfetti = false },
            modifier = Modifier.fillMaxSize()
        )

        // AI Spaced Repetition Study Plan Generator Modal
        if (showAiGeneratorDialog) {
            AiPlanGeneratorDialog(
                cbseClass = activeProfile.cbseClass,
                onGenerate = { topic, subject, examDays, dailyMinutes, confidence ->
                    HapticHelper.triggerImpact(haptic)
                    repository.generateCustomSpacedSchedule(topic, subject, examDays, dailyMinutes, confidence)
                    showConfetti = true
                    showAiGeneratorDialog = false
                },
                onDismiss = { showAiGeneratorDialog = false }
            )
        }
    }
}

@Composable
fun DraggableTaskCard(
    task: RevisionTask,
    index: Int,
    totalCount: Int,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPostpone: () -> Unit,
    onOpenChapter: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dragScale"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .scale(scale)
            .pointerInput(task.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        HapticHelper.triggerImpact(haptic)
                        isDragging = true
                    },
                    onDragEnd = {
                        isDragging = false
                        if (dragOffsetY < -60f) {
                            onMoveUp()
                        } else if (dragOffsetY > 60f) {
                            onMoveDown()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-120f, 120f)
                    }
                )
            },
        backgroundColor = if (task.isCompleted) LiquidSurface.copy(alpha = 0.45f) else LiquidSurfaceGlass,
        glowAccentColor = if (isDragging) LiquidGlowCyan else if (!task.isCompleted) LiquidGlowIndigo else null,
        borderWidth = if (isDragging) 2.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle icon
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = if (isDragging) LiquidGlowCyan else TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )

                // Checkbox with animated scale pop & haptic checkmark
                Box(
                    modifier = Modifier
                        .scale(checkScale)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) LiquidGlowEmerald else LiquidSurfaceElevated)
                        .border(
                            1.5.dp,
                            if (task.isCompleted) LiquidGlowEmerald else LiquidCardBorder,
                            CircleShape
                        )
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${task.taskType.emoji} ${task.taskType.label}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${task.subjectName}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = task.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) TextMuted else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⏱️ ${task.estimatedMinutes}m",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ +${task.xpReward} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowAmber
                        )
                    }
                }

                // Reorder arrows & postpone buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (index > 0) TextSecondary else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) TextSecondary else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenChapter,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = "Open Chapter",
                            tint = LiquidGlowCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiPlanGeneratorDialog(
    cbseClass: Int,
    onGenerate: (topic: String, subject: String, examDays: Int, dailyMinutes: Int, confidence: String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var topicInput by remember { mutableStateOf("Chemical Reactions & Life Processes") }
    var selectedSubject by remember { mutableStateOf("Science") }
    var selectedExamDays by remember { mutableIntStateOf(14) }
    var selectedMinutes by remember { mutableIntStateOf(45) }
    var selectedConfidence by remember { mutableStateOf("Intermediate") }

    val subjects = listOf("Science", "Mathematics", "Social Science", "Physics", "Chemistry")
    val examDaysOptions = listOf(7, 14, 21, 30)
    val minuteOptions = listOf(30, 45, 60, 90)
    val confidenceOptions = listOf("Needs Work", "Intermediate", "Strong Recall")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LiquidSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LiquidGlowCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Spaced Repetition Engine",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Generate a calibrated SuperMemo SM-2 daily study schedule for CBSE Class $cbseClass:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Topic input
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text("Topic or Chapter Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiquidGlowIndigo,
                        unfocusedBorderColor = LiquidCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Subject pills
                Text(text = "Select Subject:", fontSize = 11.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects.size) { i ->
                        val subj = subjects[i]
                        val isSelected = subj == selectedSubject
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LiquidGlowCyan else LiquidSurfaceElevated)
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    selectedSubject = subj
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = subj,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                // Days until exam
                Text(text = "Days Until Target Exam:", fontSize = 11.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    examDaysOptions.forEach { days ->
                        val isSelected = days == selectedExamDays
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    selectedExamDays = days
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${days}d",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                // Daily study budget
                Text(text = "Daily Revision Budget:", fontSize = 11.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    minuteOptions.forEach { mins ->
                        val isSelected = mins == selectedMinutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LiquidGlowAmber else LiquidSurfaceElevated)
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    selectedMinutes = mins
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                // Current Confidence
                Text(text = "Current Mastery Level:", fontSize = 11.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    confidenceOptions.forEach { conf ->
                        val isSelected = conf == selectedConfidence
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LiquidGlowEmerald else LiquidSurfaceElevated)
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    selectedConfidence = conf
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conf,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerate(topicInput, selectedSubject, selectedExamDays, selectedMinutes, selectedConfidence)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate SM-2 Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
