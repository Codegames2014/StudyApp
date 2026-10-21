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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NcertDatabase
import com.example.data.model.NcertChapter
import com.example.data.model.StudentProfile
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.components.LiquidWaveHeader
import com.example.ui.components.MistakeVaultSheet
import com.example.ui.components.WeaknessRadarChart
import com.example.data.local.StudyFluidRepository
import com.example.ui.components.FluidThemeSelectorCard
import com.example.ui.components.FocusStreakCard
import com.example.ui.components.PomodoroTimerCard
import com.example.ui.components.XPMeter
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    profile: StudentProfile,
    onOpenProfileSwitcher: () -> Unit,
    onNavigateToChapter: (String) -> Unit,
    onNavigateToNotebookLm: (String) -> Unit,
    onNavigateToTutor: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToMastery: (() -> Unit)? = null,
    onNavigateToPlanner: (() -> Unit)? = null,
    repository: StudyFluidRepository? = null
) {
    val haptic = LocalHapticFeedback.current
    var showMistakeVaultSheet by remember { mutableStateOf(false) }
    val chapters = NcertDatabase.getAllChaptersForClass(profile.cbseClass).ifEmpty {
        listOf(NcertDatabase.class10ChemicalReactions)
    }
    val featuredChapter = chapters.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidBackground)
    ) {
        // Decorative Liquid Wave Header
        LiquidWaveHeader(
            modifier = Modifier.align(Alignment.TopCenter),
            height = 140.dp,
            primaryColor = Color(profile.avatarColorHex),
            secondaryColor = LiquidGlowCyan
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- TOP BAR: Profile Switcher Pill & Streak ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Switcher Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(LiquidSurfaceElevated)
                            .border(1.dp, Color(profile.avatarColorHex).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                            .clickable {
                                HapticHelper.triggerSelection(haptic)
                                onOpenProfileSwitcher()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("btn_profile_switcher_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(profile.avatarColorHex).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = profile.avatarEmoji, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Switch profile",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = profile.displayClass,
                                fontSize = 11.sp,
                                color = Color(profile.avatarColorHex)
                            )
                        }
                    }

                    // Streak & XP Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Streak
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(LiquidGlowAmber.copy(alpha = 0.15f))
                                .border(1.dp, LiquidGlowAmber.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔥", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile.studyStreakDays}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowAmber
                            )
                        }

                        // Knowledge Points (XP)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(LiquidGlowIndigo.copy(alpha = 0.15f))
                                .border(1.dp, LiquidGlowIndigo.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⚡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile.knowledgePoints} XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowIndigo
                            )
                        }
                    }
                }
            }

            // --- MASTERY LEVEL & XP PROGRESS METER ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag("home_xp_meter_card"),
                    glowAccentColor = LiquidGlowAmber,
                    onClick = onNavigateToMastery
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        XPMeter(
                            currentXp = profile.knowledgePoints,
                            pointsPerLevel = 150,
                            modifier = Modifier.fillMaxWidth(),
                            barHeight = 8.dp,
                            showRankTitle = true,
                            onClick = onNavigateToMastery
                        )
                    }
                }
            }

            // --- MODULE 3: HOME DASHBOARD 'FOCUS STREAK' COUNTER ---
            item {
                FocusStreakCard(
                    streakDays = profile.studyStreakDays,
                    dailyGoalMinutes = 60,
                    minutesStudiedToday = 45,
                    knowledgePoints = profile.knowledgePoints,
                    onClick = {
                        if (repository != null) {
                            repository.incrementStreakAndXp(10)
                        }
                    }
                )
            }

            // --- MODULE 4: GLASSMORPHIC POMODORO STUDY TIMER ---
            item {
                PomodoroTimerCard(
                    onSessionCompleted = { mode ->
                        if (repository != null && mode.defaultMinutes >= 15) {
                            repository.incrementStreakAndXp(35)
                        }
                    }
                )
            }

            // --- FLUID THEME ENGINE SELECTOR ---
            if (repository != null) {
                item {
                    FluidThemeSelectorCard(repository = repository)
                }
            }

            // --- HERO BANNER: CBSE Motivation & Socratic FluidTutor ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag("hero_banner_tutor"),
                    glowAccentColor = LiquidGlowIndigo,
                    onClick = onNavigateToTutor
                ) {

                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "AI TUTOR • GEMINI GROUNDED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidGlowIndigo
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Have a doubt in your NCERT chapter?",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Ask FluidTutor for Socratic step-by-step breakdown or scan textbook diagrams instantly.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(LiquidGlowIndigo.copy(alpha = 0.4f), Color.Transparent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = LiquidGlowIndigo,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LiquidSurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ask anything e.g. 'Explain Redox reaction'...",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = LiquidGlowCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // --- NOTEBOOKLM FEATURE CARDS (Deep Dive Podcast & Studio) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NotebookLM Studio",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Grounded RAG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LiquidGlowCyan
                    )
                }
            }

            item {
                // Featured Deep Dive Chapter Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag("card_featured_deep_dive"),
                    glowAccentColor = LiquidGlowCyan,
                    onClick = { onNavigateToNotebookLm(featuredChapter.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(LiquidGlowCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Podcasts,
                                        contentDescription = null,
                                        tint = LiquidGlowCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "AUDIO OVERVIEW • TWO-HOST PODCAST",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidGlowCyan
                                    )
                                    Text(
                                        text = "Ch ${featuredChapter.chapterNumber}: ${featuredChapter.title}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = featuredChapter.summary,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CitationBadge(text = "Host Aarav & Priya")
                                CitationBadge(text = "${featuredChapter.estimatedReadMinutes} min")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiquidGlowCyan)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Play Deep Dive",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // --- 4-PILLAR NOTEBOOKLM QUICK LAUNCHERS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickToolCard(
                        title = "Flashcards",
                        subtitle = "3D Recall Flip",
                        icon = Icons.Default.Style,
                        glowColor = LiquidGlowViolet,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToNotebookLm(featuredChapter.id) }
                    )

                    QuickToolCard(
                        title = "MCQ Quiz",
                        subtitle = "CBSE Marking",
                        icon = Icons.Default.FactCheck,
                        glowColor = LiquidGlowEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToNotebookLm(featuredChapter.id) }
                    )

                    QuickToolCard(
                        title = "Mind Map",
                        subtitle = "Concept Tree",
                        icon = Icons.Default.AccountTree,
                        glowColor = LiquidGlowAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToNotebookLm(featuredChapter.id) }
                    )
                }
            }

            // --- MASTERY MAP & AI PLANNER LAUNCHERS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        glowAccentColor = LiquidGlowAmber,
                        onClick = { onNavigateToMastery?.invoke() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LiquidGlowAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = LiquidGlowAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Mastery Map",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "CBSE Skill Tree",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    GlassCard(
                        modifier = Modifier.weight(1f),
                        glowAccentColor = LiquidGlowCyan,
                        onClick = { onNavigateToPlanner?.invoke() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LiquidGlowCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = LiquidGlowCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Planner",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Spaced Recall",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // --- WEAKNESS RADAR & MISTAKE VAULT ---
            if (repository != null) {
                item {
                    val mistakes by repository.getMistakesForActiveProfile().collectAsState(initial = emptyList())
                    val allProfiles by repository.profiles.collectAsState()
                    WeaknessRadarChart(
                        mistakes = mistakes,
                        studentName = profile.name,
                        cbseClass = profile.cbseClass,
                        profiles = allProfiles,
                        activeProfileId = profile.id,
                        onSwitchProfile = { newId -> repository.switchProfile(newId) },
                        onDrillDownSubject = {
                            showMistakeVaultSheet = true
                        }
                    )
                }
            }

            // --- NCERT TEXTBOOK VAULT CURRICULUM ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CBSE Class ${profile.cbseClass} NCERT Vault",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LiquidGlowIndigo,
                        modifier = Modifier.clickable { onNavigateToVault() }
                    )
                }
            }

            items(chapters) { ch ->
                ChapterRowCard(
                    chapter = ch,
                    isBookmarked = profile.bookmarkedChapterIds.contains(ch.id),
                    onClick = { onNavigateToChapter(ch.id) }
                )
            }
        }

        if (showMistakeVaultSheet && repository != null) {
            MistakeVaultSheet(
                repository = repository,
                onDismiss = { showMistakeVaultSheet = false }
            )
        }
    }
}

@Composable
fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        glowAccentColor = glowColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ChapterRowCard(
    chapter: NcertChapter,
    isBookmarked: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LiquidSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${chapter.chapterNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidGlowIndigo
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chapter.subjectName,
                        fontSize = 11.sp,
                        color = LiquidGlowCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${chapter.citations.size} Citations",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            if (isBookmarked) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmarked",
                    tint = LiquidGlowAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CitationBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(LiquidSurfaceElevated)
            .border(1.dp, LiquidCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}
