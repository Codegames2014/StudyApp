package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NcertDatabase
import com.example.data.local.StudyFluidRepository
import com.example.data.model.*
import com.example.ui.components.CameraScannerSheet
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.components.OfflineConnectionState
import com.example.ui.components.OfflineStatusBadge
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookLmScreen(
    chapterId: String,
    repository: StudyFluidRepository,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val chapter = remember(chapterId) {
        NcertDatabase.getChapterById(chapterId) ?: NcertDatabase.class10ChemicalReactions
    }
    val activeProfile = repository.activeProfile

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Audio Overview", "Grounded RAG", "Study Guide", "Study Aids", "Notes")
    var showCameraScanner by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var celebrationTitle by remember { mutableStateOf("Chapter Mastered!") }
    var celebrationSubtitle by remember { mutableStateOf("Knowledge XP Awarded ⚡") }

    // --- TTS Player State for Audio Overview Podcast ---
    var isTtsPlaying by remember { mutableStateOf(false) }
    var currentDialogueIndex by remember { mutableIntStateOf(0) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.ENGLISH
                ttsInstance?.setSpeechRate(playbackSpeed)
            }
        }
        textToSpeech = ttsInstance

        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    // Play next dialogue line
    fun speakCurrentLine(index: Int) {
        val dialogueList = chapter.podcastScript
        if (dialogueList.isEmpty()) return
        val item = dialogueList.getOrNull(index) ?: return

        textToSpeech?.stop()
        textToSpeech?.setSpeechRate(playbackSpeed)
        // Adjust pitch slightly for host distinctiveness (Aarav slightly deeper, Priya clearer)
        if (item.speaker == "Aarav") {
            textToSpeech?.setPitch(0.95f)
        } else {
            textToSpeech?.setPitch(1.15f)
        }

        val params = android.os.Bundle()
        textToSpeech?.speak(item.text, TextToSpeech.QUEUE_FLUSH, params, "dialogue_$index")
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (index + 1 < dialogueList.size && isTtsPlaying) {
                    currentDialogueIndex = index + 1
                    speakCurrentLine(index + 1)
                } else {
                    isTtsPlaying = false
                }
            }
            override fun onError(utteranceId: String?) {
                isTtsPlaying = false
            }
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    textToSpeech?.stop()
                    onBack()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NotebookLM Vault",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlowCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• CBSE Cl.${chapter.cbseClass}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = chapter.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
            }

            val cachedIds by repository.cachedChapterIds.collectAsState()
            val downloadProgressMap by repository.downloadProgress.collectAsState()
            val isCached = cachedIds.contains(chapter.id)
            val downloadProgress = downloadProgressMap[chapter.id]

            val connectionState = when {
                downloadProgress != null -> OfflineConnectionState.DOWNLOADING
                isCached -> OfflineConnectionState.OFFLINE_CACHED
                else -> OfflineConnectionState.ONLINE_CLOUD
            }

            // Fluid morphing OfflineStatusBadge
            OfflineStatusBadge(
                state = connectionState,
                downloadProgress = downloadProgress,
                onToggle = {
                    if (isCached) repository.removeChapterFromCache(chapter.id)
                    else repository.downloadChapterForOffline(chapter.id)
                }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Camera Doubt Scanner icon in NotebookLM
            IconButton(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    showCameraScanner = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Camera Scanner",
                    tint = LiquidGlowCyan
                )
            }

            IconButton(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    repository.toggleBookmarkChapter(chapter.id)
                }
            ) {
                val isBookmarked = activeProfile.bookmarkedChapterIds.contains(chapter.id)
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) LiquidGlowAmber else TextSecondary
                )
            }
        }

        // --- Fluid Horizontal Tab Bar ---
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = LiquidSurface,
            contentColor = LiquidGlowIndigo,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        selectedTabIndex = index
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) LiquidGlowCyan else TextSecondary
                        )
                    }
                )
            }
        }

        // --- Tab Content Router ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> AudioOverviewTab(
                    chapter = chapter,
                    isPlaying = isTtsPlaying,
                    currentDialogueIndex = currentDialogueIndex,
                    playbackSpeed = playbackSpeed,
                    onTogglePlay = {
                        HapticHelper.triggerImpact(haptic)
                        if (isTtsPlaying) {
                            textToSpeech?.stop()
                            isTtsPlaying = false
                        } else {
                            isTtsPlaying = true
                            speakCurrentLine(currentDialogueIndex)
                        }
                    },
                    onSelectDialogue = { idx ->
                        HapticHelper.triggerSelection(haptic)
                        currentDialogueIndex = idx
                        isTtsPlaying = true
                        speakCurrentLine(idx)
                    },
                    onChangeSpeed = { spd ->
                        playbackSpeed = spd
                        textToSpeech?.setSpeechRate(spd)
                    }
                )

                1 -> GroundedRagTab(
                    chapter = chapter,
                    studentName = activeProfile.name
                )

                2 -> StudyGuideTab(
                    chapter = chapter
                )

                3 -> StudyAidsTab(
                    chapter = chapter,
                    onEarnXp = { xp ->
                        repository.incrementStreakAndXp(xp)
                        celebrationTitle = if (xp >= 30) "Quiz Completed!" else "Concept Mastered!"
                        celebrationSubtitle = "+$xp Knowledge XP Awarded ⚡"
                        showCelebration = true
                    }
                )

                4 -> NotesTab(
                    chapterId = chapter.id,
                    repository = repository
                )
            }
        }
    }

        if (showCameraScanner) {
            CameraScannerSheet(
                studentName = activeProfile.name,
                cbseClass = activeProfile.cbseClass,
                activeChapterTitle = chapter.title,
                onSaveToNotes = { title, solution, citation ->
                    repository.addNote(chapter.id, title, solution, citation)
                },
                onDismiss = { showCameraScanner = false }
            )
        }

        CelebrationOverlay(
            trigger = showCelebration,
            title = celebrationTitle,
            subtitle = celebrationSubtitle,
            onDismiss = { showCelebration = false }
        )
    }
}

// ==========================================
// 1. AUDIO OVERVIEW (PODCAST) TAB
// ==========================================
@Composable
fun AudioOverviewTab(
    chapter: NcertChapter,
    isPlaying: Boolean,
    currentDialogueIndex: Int,
    playbackSpeed: Float,
    onTogglePlay: () -> Unit,
    onSelectDialogue: (Int) -> Unit,
    onChangeSpeed: (Float) -> Unit
) {
    val script = chapter.podcastScript.ifEmpty {
        listOf(
            PodcastDialogue("Aarav", "Concept Host", "Welcome to StudyFluid Deep Dive on ${chapter.title}. Today Priya and I will break down the NCERT syllabus into high-yield exam insights."),
            PodcastDialogue("Priya", "Exam Strategist", "That's right Aarav! We're focusing on definitions, key chemical & physical laws, and board question patterns.")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Player Console
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth().testTag("podcast_player_console"),
                glowAccentColor = LiquidGlowCyan
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "THE DEEP DIVE • TWO-HOST AUDIO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowCyan
                            )
                            Text(
                                text = "Aarav & Priya Discussion",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Speed Pill
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1.0f, 1.25f, 1.5f).forEach { spd ->
                                val isSelected = playbackSpeed == spd
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) LiquidGlowCyan else LiquidSurfaceElevated)
                                        .clickable { onChangeSpeed(spd) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${spd}x",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Waveform visualizer
                    WaveformVisualizer(
                        isPlaying = isPlaying,
                        height = 64.dp,
                        primaryColor = LiquidGlowIndigo,
                        secondaryColor = LiquidGlowCyan
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentDialogueIndex > 0) onSelectDialogue(currentDialogueIndex - 1)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous turn",
                                tint = TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(LiquidGlowCyan)
                                .clickable { onTogglePlay() }
                                .testTag("btn_podcast_toggle_play"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = {
                                if (currentDialogueIndex + 1 < script.size) onSelectDialogue(currentDialogueIndex + 1)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next turn",
                                tint = TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dialogue Transcript Section
        item {
            Text(
                text = "Live Episode Transcript",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        itemsIndexed(script) { idx, dialogue ->
            val isCurrent = idx == currentDialogueIndex
            val isAarav = dialogue.speaker == "Aarav"

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDialogue(idx) },
                backgroundColor = if (isCurrent) LiquidSurfaceElevated else LiquidSurfaceGlass,
                glowAccentColor = if (isCurrent) (if (isAarav) LiquidGlowIndigo else LiquidGlowCyan) else null,
                borderWidth = if (isCurrent) 1.5.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Speaker Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAarav) LiquidGlowIndigo.copy(alpha = 0.25f)
                                else LiquidGlowCyan.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAarav) "👨‍🏫" else "👩‍🔬",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${dialogue.speaker} (${dialogue.avatarRole})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAarav) LiquidGlowIndigo else LiquidGlowCyan
                            )
                            if (isCurrent && isPlaying) {
                                Text(
                                    text = "SPEAKING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGlowEmerald
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = dialogue.text,
                            fontSize = 13.sp,
                            color = if (isCurrent) TextPrimary else TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. GROUNDED RAG TAB
// ==========================================
@Composable
fun GroundedRagTab(
    chapter: NcertChapter,
    studentName: String
) {
    val haptic = LocalHapticFeedback.current
    var ragQuery by remember { mutableStateOf("") }
    var selectedCitation by remember { mutableStateOf<NcertCitation?>(null) }

    val citations = chapter.citations

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowIndigo
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MULTI-SOURCE GROUNDED RAG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlowIndigo
                    )
                    Text(
                        text = "Query NCERT Ch.${chapter.chapterNumber} Vault",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Every answer is verified against official NCERT textbook pages with exact citation coordinates.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = ragQuery,
                        onValueChange = { ragQuery = it },
                        placeholder = { Text("Search concepts e.g. 'electrolysis', 'mirror formula'...") },
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = LiquidGlowCyan)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_rag_search"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidGlowCyan,
                            unfocusedBorderColor = LiquidCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Text(
                text = "Extracted NCERT Textbook Citations (${citations.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(citations) { cit ->
            val isSelected = selectedCitation == cit
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticHelper.triggerSelection(haptic)
                        selectedCitation = if (isSelected) null else cit
                    },
                glowAccentColor = if (isSelected) LiquidGlowCyan else null,
                borderWidth = if (isSelected) 1.5.dp else 1.dp
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
                                .background(LiquidGlowIndigo.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = cit.citationBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowIndigo
                            )
                        }

                        Text(
                            text = "Page ${cit.pageNumber}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = cit.sectionTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"${cit.excerpt}\"",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. STUDY GUIDE TAB
// ==========================================
@Composable
fun StudyGuideTab(chapter: NcertChapter) {
    val guide = chapter.studyGuide

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive Summary
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowAmber
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Summarize, contentDescription = null, tint = LiquidGlowAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated Executive Briefing",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = guide.executiveSummary,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Key Formulas and Laws Cheat Sheet
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowCyan
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Functions, contentDescription = null, tint = LiquidGlowCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Key Formulas & Governing Laws",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    chapter.keyFormulasAndLaws.forEach { formula ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = "• ", color = LiquidGlowCyan, fontWeight = FontWeight.Bold)
                            Text(text = formula, fontSize = 12.sp, color = TextPrimary, lineHeight = 17.sp)
                        }
                    }
                }
            }
        }

        // CBSE Board Exam Watchouts & Pitfalls
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowRose
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = LiquidGlowRose)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CBSE Marking Scheme Watchouts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    guide.examWatchouts.forEach { pitfall ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = "⚠️ ", fontSize = 12.sp)
                            Text(text = pitfall, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }

        // Historical Timeline & Context
        if (guide.historicalContextOrTimeline.isNotEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowAccentColor = LiquidGlowViolet
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.HistoryEdu, contentDescription = null, tint = LiquidGlowViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Historical Timeline & Scientific Milestones",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        guide.historicalContextOrTimeline.forEach { event ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(text = "⏱️ ", fontSize = 12.sp)
                                Text(text = event, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. STUDY AIDS TAB (Flashcards, Quiz, Mind Map)
// ==========================================
@Composable
fun StudyAidsTab(
    chapter: NcertChapter,
    onEarnXp: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var subTab by remember { mutableIntStateOf(0) } // 0: Flashcards, 1: MCQ Quiz, 2: Mind Map

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // Sub-selector pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Flashcards", "MCQ Quiz", "Mind Map").forEachIndexed { idx, name ->
                val isSelected = subTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                        .clickable {
                            HapticHelper.triggerSelection(haptic)
                            subTab = idx
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (subTab) {
            0 -> FlashcardSubSection(
                flashcards = chapter.flashcards,
                onCardMastered = { onEarnXp(15) }
            )
            1 -> QuizSubSection(
                questions = chapter.quizQuestions,
                onCompleteQuiz = { score -> onEarnXp(score * 20) }
            )
            2 -> MindMapSubSection(rootNode = chapter.mindMapRoot)
        }
    }
}

@Composable
fun FlashcardSubSection(
    flashcards: List<FlashCard>,
    onCardMastered: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    if (flashcards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No flashcards available for this module yet.", color = TextMuted)
        }
        return
    }

    var cardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val currentCard = flashcards[cardIndex]

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "flashcardFlip"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Card ${cardIndex + 1} of ${flashcards.size}",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = "Tap to flip (3D Rotation)",
                fontSize = 11.sp,
                color = LiquidGlowCyan
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3D Flip Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clip(RoundedCornerShape(20.dp))
                .background(LiquidSurfaceElevated)
                .border(1.5.dp, if (isFlipped) LiquidGlowCyan else LiquidGlowIndigo, RoundedCornerShape(20.dp))
                .clickable {
                    HapticHelper.triggerImpact(haptic)
                    isFlipped = !isFlipped
                }
                .testTag("flashcard_flip_box")
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LiquidGlowIndigo.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = currentCard.category, fontSize = 11.sp, color = LiquidGlowIndigo, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentCard.frontPrompt,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            } else {
                // Back (Mirrored to uninvert text)
                Column(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LiquidGlowCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = currentCard.ncertCitation, fontSize = 11.sp, color = LiquidGlowCyan, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentCard.backAnswer,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation & Recall Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    HapticHelper.triggerSelection(haptic)
                    isFlipped = false
                    if (cardIndex > 0) cardIndex--
                },
                modifier = Modifier.weight(1f),
                enabled = cardIndex > 0,
                colors = ButtonDefaults.buttonColors(containerColor = LiquidSurfaceElevated)
            ) {
                Text("Previous")
            }

            Button(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    onCardMastered()
                    isFlipped = false
                    if (cardIndex + 1 < flashcards.size) {
                        cardIndex++
                    } else {
                        cardIndex = 0
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowEmerald)
            ) {
                Text("Mastered (+15 XP)", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuizSubSection(
    questions: List<QuizQuestion>,
    onCompleteQuiz: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz questions generated for this chapter yet.", color = TextMuted)
        }
        return
    }

    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var isSubmitted by remember { mutableStateOf(false) }

    val q = questions[currentQuestionIdx]

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CBSE MCQ Practice • Q${currentQuestionIdx + 1}/${questions.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidGlowIndigo
            )
            Text(text = "Score: $score", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidGlowEmerald)
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = q.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = q.ncertReference,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        q.options.forEachIndexed { idx, opt ->
            val isSelected = selectedOptionIdx == idx
            val isCorrect = idx == q.correctIndex

            val backgroundColor = when {
                !isSubmitted && isSelected -> LiquidGlowIndigo.copy(alpha = 0.25f)
                isSubmitted && isCorrect -> LiquidGlowEmerald.copy(alpha = 0.35f)
                isSubmitted && isSelected && !isCorrect -> LiquidGlowRose.copy(alpha = 0.35f)
                else -> LiquidSurfaceElevated
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = when {
                            isSubmitted && isCorrect -> LiquidGlowEmerald
                            isSubmitted && isSelected && !isCorrect -> LiquidGlowRose
                            isSelected -> LiquidGlowIndigo
                            else -> LiquidCardBorder
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = !isSubmitted) {
                        HapticHelper.triggerSelection(haptic)
                        selectedOptionIdx = idx
                    }
                    .padding(14.dp)
            ) {
                Text(
                    text = "${('A' + idx)}. $opt",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }
        }

        if (isSubmitted) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowEmerald
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Detailed NCERT Explanation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlowEmerald
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = q.explanation,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isSubmitted) {
            Button(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    isSubmitted = true
                    if (selectedOptionIdx == q.correctIndex) {
                        score++
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = selectedOptionIdx != null,
                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
            ) {
                Text("Check Answer", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = {
                    HapticHelper.triggerSelection(haptic)
                    if (currentQuestionIdx + 1 < questions.size) {
                        currentQuestionIdx++
                        selectedOptionIdx = null
                        isSubmitted = false
                    } else {
                        onCompleteQuiz(score)
                        // Reset quiz
                        currentQuestionIdx = 0
                        selectedOptionIdx = null
                        isSubmitted = false
                        score = 0
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowCyan)
            ) {
                Text(
                    text = if (currentQuestionIdx + 1 < questions.size) "Next Question" else "Complete Quiz & Claim XP",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MindMapSubSection(rootNode: MindMapNode) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowIndigo
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountTree, contentDescription = null, tint = LiquidGlowIndigo)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rootNode.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    rootNode.subtext?.let {
                        Text(text = it, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        items(rootNode.children) { child ->
            MindMapChildNodeView(node = child)
        }
    }
}

@Composable
fun MindMapChildNodeView(node: MindMapNode) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "↳ ${node.label}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiquidGlowCyan
            )
            node.subtext?.let {
                Text(text = it, fontSize = 11.sp, color = TextMuted)
            }

            if (node.children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    node.children.forEach { grandChild ->
                        Text(
                            text = "• ${grandChild.label}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PINNED NOTES TAB
// ==========================================
@Composable
fun NotesTab(
    chapterId: String,
    repository: StudyFluidRepository
) {
    val haptic = LocalHapticFeedback.current
    val notes by repository.notes.collectAsState()
    val chapterNotes = notes.filter { it.chapterId == chapterId }

    var isAddingNote by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Isolated Chapter Vault Notes",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Button(
                onClick = {
                    HapticHelper.triggerSelection(haptic)
                    isAddingNote = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo.copy(alpha = 0.3f))
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Note", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isAddingNote) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "New Pinned Note", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Key Formula or Takeaway") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { isAddingNote = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                    HapticHelper.triggerImpact(haptic)
                                    repository.addNote(chapterId, noteTitle, noteContent, "NCERT Note")
                                    noteTitle = ""
                                    noteContent = ""
                                    isAddingNote = false
                                }
                            }
                        ) {
                            Text("Save Note")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (chapterNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notes added for this chapter yet. Jot down formulas or exam tricks!", color = TextMuted)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(chapterNotes) { note ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                IconButton(
                                    onClick = {
                                        HapticHelper.triggerSelection(haptic)
                                        repository.deleteNote(note.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = note.content,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "🏷️ ${note.ncertCitationTag}",
                                fontSize = 10.sp,
                                color = LiquidGlowCyan
                            )
                        }
                    }
                }
            }
        }
    }
}
