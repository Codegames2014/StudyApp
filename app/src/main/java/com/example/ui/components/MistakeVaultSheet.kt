package com.example.ui.components

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.local.room.MistakeEntity
import com.example.data.remote.GeminiService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeVaultSheet(
    repository: StudyFluidRepository,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val activeProfile = repository.activeProfile

    val allMistakes by repository.getMistakesForActiveProfile().collectAsState(initial = emptyList())
    val unmasteredMistakes = allMistakes.filter { !it.isMastered }

    var weekendDrillText by remember { mutableStateOf<String?>(null) }
    var isGeneratingDrill by remember { mutableStateOf(false) }
    var showDrillDialog by remember { mutableStateOf(false) }
    var selectedSubjectFilter by remember { mutableStateOf("All") }

    val filteredMistakes = if (selectedSubjectFilter == "All") {
        allMistakes
    } else {
        allMistakes.filter { it.subject.equals(selectedSubjectFilter, ignoreCase = true) }
    }

    // Weakness Radar metrics: calculate percentage mastery per subject
    val subjects = listOf("Physics", "Chemistry", "Biology", "Mathematics", "Social Science")
    val subjectMasteryRatios = subjects.map { subj ->
        val subjMistakes = allMistakes.filter { it.subject.equals(subj, ignoreCase = true) }
        if (subjMistakes.isEmpty()) 0.85f // Default strong
        else {
            val masteredCount = subjMistakes.count { it.isMastered }
            (masteredCount.toFloat() / subjMistakes.size).coerceIn(0.2f, 1.0f)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0A0F),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LiquidGlowRose.copy(alpha = 0.2f))
                            .border(1.dp, LiquidGlowRose, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎯", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mistake Vault & Radar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LiquidGlowRose.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${unmasteredMistakes.size} DEFICIENCIES",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGlowRose
                                )
                            }
                        }
                        Text(
                            text = "Room-backed tracking for targeted concept healing",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LiquidSurfaceElevated)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val allProfiles by repository.profiles.collectAsState()
            WeaknessRadarChart(
                mistakes = allMistakes,
                studentName = activeProfile.name,
                cbseClass = activeProfile.cbseClass,
                profiles = allProfiles,
                activeProfileId = activeProfile.id,
                onSwitchProfile = { repository.switchProfile(it) },
                onDrillDownSubject = { subject ->
                    selectedSubjectFilter = subject
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI Weekend Test Prompt Banner
            Button(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    isGeneratingDrill = true
                    coroutineScope.launch {
                        val drill = GeminiService.generateWeekendRevisionTest(
                            mistakes = unmasteredMistakes,
                            studentName = activeProfile.name
                        )
                        isGeneratingDrill = false
                        weekendDrillText = drill
                        showDrillDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_generate_mistake_drill"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidGlowIndigo
                )
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGeneratingDrill) "Gemini Formulating Drill..." else "Generate Weekend Mistake Healing Drill",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Chemistry", "Physics", "Biology", "Mathematics").forEach { subj ->
                    val isSelected = selectedSubjectFilter == subj
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LiquidSurfaceElevated else LiquidSurfaceGlass)
                            .border(1.dp, if (isSelected) LiquidGlowCyan else LiquidCardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                HapticHelper.triggerSelection(haptic)
                                selectedSubjectFilter = subj
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subj,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) LiquidGlowCyan else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mistake Items List from Room
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMistakes) { mistake ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowAccentColor = if (mistake.isMastered) LiquidGlowEmerald else LiquidGlowRose
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (mistake.isMastered) LiquidGlowEmerald.copy(alpha = 0.2f)
                                                else LiquidGlowRose.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (mistake.isMastered) "MASTERED ⚡" else mistake.mistakeCategory.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (mistake.isMastered) LiquidGlowEmerald else LiquidGlowRose
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = mistake.subject, fontSize = 11.sp, color = TextMuted)
                                }

                                if (!mistake.isMastered) {
                                    Button(
                                        onClick = {
                                            HapticHelper.triggerImpact(haptic)
                                            coroutineScope.launch {
                                                repository.healMistake(mistake.id)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowEmerald.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("btn_heal_mistake_${mistake.id}")
                                    ) {
                                        Text("Heal +40 XP", fontSize = 11.sp, color = LiquidGlowEmerald, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = mistake.questionText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Student Mistake vs Correct Answer
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "❌ Past Error:", fontSize = 10.sp, color = LiquidGlowRose)
                                    Text(text = mistake.studentAnswer, fontSize = 11.sp, color = TextSecondary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "✅ NCERT Solution:", fontSize = 10.sp, color = LiquidGlowEmerald)
                                    Text(text = mistake.correctAnswer, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "💡 ${mistake.explanation}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Weekend Drill Modal Dialog
    if (showDrillDialog && weekendDrillText != null) {
        AlertDialog(
            onDismissRequest = { showDrillDialog = false },
            containerColor = Color(0xFF14141E),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎯 Weekend Healing Test", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidGlowCyan)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    item {
                        Text(
                            text = weekendDrillText!!,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.triggerImpact(haptic)
                        showDrillDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
                ) {
                    Text("Got It! Start Practice")
                }
            }
        )
    }
}
