package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.NcertDatabase
import com.example.data.local.StudyFluidRepository
import com.example.data.model.NcertChapter
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.components.OfflineConnectionState
import com.example.ui.components.OfflineStatusBadge
import com.example.ui.theme.*

@Composable
fun NcertVaultScreen(
    repository: StudyFluidRepository,
    onNavigateToChapterNotebook: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val activeProfile = repository.activeProfile

    var selectedClass by remember { mutableIntStateOf(activeProfile.cbseClass) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyOffline by remember { mutableStateOf(false) }

    val cachedChapterIds by repository.cachedChapterIds.collectAsState()
    val downloadProgressMap by repository.downloadProgress.collectAsState()

    val subjects = NcertDatabase.getSubjectsForClass(selectedClass)
    val chapters = if (selectedSubjectId != null) {
        subjects.find { it.id == selectedSubjectId }?.chapters ?: emptyList()
    } else {
        subjects.flatMap { it.chapters }
    }.filter { ch ->
        val matchesQuery = if (searchQuery.isBlank()) true
        else ch.title.contains(searchQuery, ignoreCase = true) || ch.summary.contains(searchQuery, ignoreCase = true)

        val matchesOffline = if (showOnlyOffline) cachedChapterIds.contains(ch.id) else true
        matchesQuery && matchesOffline
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header & Offline Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Official NCERT Vault",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Offline-first local RAG & textbook downloads",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Fluid Morphing OfflineStatusBadge
            OfflineStatusBadge(
                state = if (showOnlyOffline) OfflineConnectionState.OFFLINE_CACHED else OfflineConnectionState.ONLINE_CLOUD,
                onToggle = {
                    showOnlyOffline = !showOnlyOffline
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search NCERT chapters or concepts...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth().testTag("input_ncert_vault_search"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LiquidGlowCyan,
                unfocusedBorderColor = LiquidCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = LiquidSurfaceElevated,
                unfocusedContainerColor = LiquidSurfaceElevated
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CBSE Class Selector Horizontal Row (1 to 12)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items((1..12).toList()) { cls ->
                val isSelected = cls == selectedClass
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                        .clickable {
                            HapticHelper.triggerSelection(haptic)
                            selectedClass = cls
                            selectedSubjectId = null
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Cl. $cls",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subject Filters
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val isAll = selectedSubjectId == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAll) LiquidGlowCyan else LiquidSurfaceGlass)
                        .clickable {
                            HapticHelper.triggerSelection(haptic)
                            selectedSubjectId = null
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "All Subjects",
                        fontSize = 11.sp,
                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAll) Color.Black else TextSecondary
                    )
                }
            }

            items(subjects) { subj ->
                val isSelected = subj.id == selectedSubjectId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(subj.colorHex) else LiquidSurfaceGlass)
                        .clickable {
                            HapticHelper.triggerSelection(haptic)
                            selectedSubjectId = subj.id
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = subj.name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chapter List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(chapters) { chapter ->
                val isOffline = cachedChapterIds.contains(chapter.id)
                val progress = downloadProgressMap[chapter.id]

                VaultChapterCardWithOffline(
                    chapter = chapter,
                    isOfflineCached = isOffline,
                    downloadProgress = progress,
                    onToggleDownload = {
                        HapticHelper.triggerImpact(haptic)
                        if (isOffline) {
                            repository.removeChapterFromCache(chapter.id)
                        } else {
                            repository.downloadChapterForOffline(chapter.id)
                        }
                    },
                    onOpenNotebookLm = { onNavigateToChapterNotebook(chapter.id) }
                )
            }
        }
    }
}

@Composable
fun VaultChapterCardWithOffline(
    chapter: NcertChapter,
    isOfflineCached: Boolean,
    downloadProgress: Float?,
    onToggleDownload: () -> Unit,
    onOpenNotebookLm: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowAccentColor = if (isOfflineCached) LiquidGlowEmerald else null,
        onClick = {
            HapticHelper.triggerSelection(haptic)
            isExpanded = !isExpanded
        }
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isOfflineCached) LiquidGlowEmerald.copy(alpha = 0.2f) else LiquidGlowIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chapter.chapterNumber}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOfflineCached) LiquidGlowEmerald else LiquidGlowIndigo
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = chapter.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = chapter.subjectName,
                                fontSize = 11.sp,
                                color = LiquidGlowCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${chapter.estimatedReadMinutes} min",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Fluid Morphing OfflineStatusBadge
                val chapterConnState = when {
                    downloadProgress != null -> OfflineConnectionState.DOWNLOADING
                    isOfflineCached -> OfflineConnectionState.OFFLINE_CACHED
                    else -> OfflineConnectionState.ONLINE_CLOUD
                }
                OfflineStatusBadge(
                    state = chapterConnState,
                    downloadProgress = downloadProgress,
                    onToggle = onToggleDownload
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = chapter.summary,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Key Formulas & Board Focus:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    chapter.keyFormulasAndLaws.take(3).forEach { law ->
                        Text(
                            text = "• $law",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenNotebookLm,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
                    ) {
                        Icon(imageVector = Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open in NotebookLM Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
