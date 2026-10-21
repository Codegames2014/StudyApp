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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.model.CosmeticTheme
import com.example.data.model.SkillNode
import com.example.ui.components.CbseExamSimulatorSheet
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.ConfettiCanvas
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.components.MistakeVaultSheet
import com.example.ui.components.XPMeter
import com.example.ui.theme.*

@Composable
fun MasteryMapScreen(
    repository: StudyFluidRepository,
    onNavigateToChapter: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val activeProfile = repository.activeProfile
    val skillNodes by repository.skillNodes.collectAsState()
    val themes by repository.cosmeticThemes.collectAsState()
    val activeThemeId by repository.activeThemeId.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMistakeVault by remember { mutableStateOf(false) }
    var showExamSimulator by remember { mutableStateOf(false) }

    // Calculate overall mastery metrics
    val masteredCount = skillNodes.count { it.isMastered }
    val totalCount = skillNodes.size
    val currentLevel = (activeProfile.knowledgePoints / 150) + 1
    val rankTitle = when {
        currentLevel >= 5 -> "CBSE Grandmaster"
        currentLevel >= 3 -> "Board Scholar"
        currentLevel >= 2 -> "Syllabus Explorer"
        else -> "NCERT Apprentice"
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
            // Header Level & XP Progress Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag("mastery_level_card"),
                    glowAccentColor = LiquidGlowAmber
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LEVEL $currentLevel",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LiquidGlowAmber
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = rankTitle,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiquidGlowIndigo
                                        )
                                    }
                                }
                                Text(
                                    text = "${activeProfile.name} • ${activeProfile.displayClass}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            // Theme Customizer Button
                            IconButton(
                                onClick = {
                                    HapticHelper.triggerSelection(haptic)
                                    showThemeDialog = true
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(LiquidSurfaceElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Cosmetic Themes",
                                    tint = LiquidGlowCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Spring-physics XPMeter with dynamic color-shifting gradients
                        XPMeter(
                            currentXp = activeProfile.knowledgePoints,
                            pointsPerLevel = 150,
                            modifier = Modifier.fillMaxWidth(),
                            barHeight = 10.dp,
                            showRankTitle = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 Chapter Concepts Mastered: $masteredCount / $totalCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Row for Weakness Radar & CBSE Mock Hall
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    HapticHelper.triggerImpact(haptic)
                                    showMistakeVault = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_mastery_mistake_vault"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowRose.copy(alpha = 0.25f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGlowRose.copy(alpha = 0.5f))
                            ) {
                                Text("🎯 Mistake Radar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidGlowRose)
                            }

                            Button(
                                onClick = {
                                    HapticHelper.triggerImpact(haptic)
                                    showExamSimulator = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_mastery_exam_hall"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowEmerald.copy(alpha = 0.25f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGlowEmerald.copy(alpha = 0.5f))
                            ) {
                                Text("🏛️ Board Exam Mock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidGlowEmerald)
                            }
                        }
                    }
                }
            }

            // Skill Tree Nodes Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CBSE Concept Skill Tree",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Complete nodes to unlock advanced exemplar chapters & liquid themes",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Interactive Skill Tree Nodes
            items(skillNodes) { node ->
                SkillNodeCard(
                    node = node,
                    onOpenChapter = { onNavigateToChapter(node.chapterId) },
                    onMasterConcept = {
                        HapticHelper.triggerImpact(haptic)
                        repository.completeSkillNode(node.id)
                        showConfetti = true
                    }
                )
            }
        }

        // Celebratory Confetti & Level-Up Overlay
        CelebrationOverlay(
            trigger = showConfetti,
            title = "Concept Mastered! 🏆",
            subtitle = "+50 Knowledge XP Awarded ⚡",
            onDismiss = { showConfetti = false },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Cosmetic Liquid Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = LiquidSurface,
            title = {
                Text(
                    text = "Cosmetic Liquid Themes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Unlock glowing neon themes by earning XP and leveling up:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    themes.forEach { theme ->
                        val isSelected = theme.id == activeThemeId
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowAccentColor = if (isSelected) Color(theme.primaryColorHex) else null,
                            borderWidth = if (isSelected) 1.5.dp else 1.dp,
                            onClick = {
                                if (theme.isUnlocked) {
                                    HapticHelper.triggerImpact(haptic)
                                    repository.setActiveTheme(theme.id)
                                    showThemeDialog = false
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(theme.primaryColorHex))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = theme.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (theme.isUnlocked) "Unlocked" else "Requires Level ${theme.unlockLevel}",
                                            fontSize = 10.sp,
                                            color = if (theme.isUnlocked) LiquidGlowEmerald else TextMuted
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = Color(theme.primaryColorHex),
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (!theme.isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done", color = LiquidGlowCyan)
                }
            }
        )
    }

    if (showMistakeVault) {
        MistakeVaultSheet(
            repository = repository,
            onDismiss = { showMistakeVault = false }
        )
    }

    if (showExamSimulator) {
        CbseExamSimulatorSheet(
            repository = repository,
            onDismiss = { showExamSimulator = false }
        )
    }
}

@Composable
fun SkillNodeCard(
    node: SkillNode,
    onOpenChapter: () -> Unit,
    onMasterConcept: () -> Unit
) {
    val tierLabel = when (node.nodeTier) {
        1 -> "Tier 1: Foundation"
        2 -> "Tier 2: Core Concept"
        3 -> "Tier 3: Board Mastery"
        else -> "Tier 4: Exemplar Champion"
    }

    val glowColor = when {
        node.isMastered -> LiquidGlowEmerald
        node.isUnlocked -> LiquidGlowIndigo
        else -> TextMuted
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowAccentColor = if (node.isUnlocked) glowColor else null,
        backgroundColor = if (node.isUnlocked) LiquidSurfaceGlass else LiquidSurface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(glowColor.copy(alpha = 0.2f))
                            .border(1.dp, glowColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (node.isUnlocked) node.iconEmoji else "🔒", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tierLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = glowColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "• ${node.subjectName}", fontSize = 10.sp, color = TextMuted)
                        }
                        Text(
                            text = node.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (node.isUnlocked) TextPrimary else TextMuted
                        )
                    }
                }

                if (node.isMastered) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "MASTERED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar & Actions
            if (node.isUnlocked) {
                LinearProgressIndicator(
                    progress = { node.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = glowColor,
                    trackColor = LiquidSurfaceElevated
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onOpenChapter) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Study Chapter", fontSize = 12.sp, color = LiquidGlowCyan)
                    }

                    if (!node.isMastered) {
                        Button(
                            onClick = onMasterConcept,
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mark Mastered (+${node.xpReward} XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "🔒 Complete prerequisite concepts in the skill tree to unlock.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
