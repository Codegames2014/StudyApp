package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.data.local.StudyFluidRepository
import com.example.data.model.NcertSubject
import com.example.ui.components.HapticHelper
import com.example.ui.components.ProfileSwitcherSheet
import com.example.ui.theme.*

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    MASTERY("Mastery", Icons.Default.AccountTree),
    VAULT("Vault", Icons.Default.MenuBook),
    PLANNER("Planner", Icons.Default.CalendarMonth),
    TUTOR("FluidTutor", Icons.Default.Psychology),
    ROOM("Rooms", Icons.Default.Groups),
    PROFILE("Profile", Icons.Default.AccountCircle)
}

@Composable
fun MainNavigation(repository: StudyFluidRepository) {

    val haptic = LocalHapticFeedback.current
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var activeNotebookChapterId by remember { mutableStateOf<String?>(null) }
    var showProfileSwitcher by remember { mutableStateOf(false) }
    var showSurveyScreen by remember { mutableStateOf(false) }

    var selectedCurriculumClass by remember { mutableStateOf<Int?>(null) }
    var selectedCurriculumSubject by remember { mutableStateOf<NcertSubject?>(null) }

    BackHandler(enabled = activeNotebookChapterId != null) {
        activeNotebookChapterId = null
    }

    BackHandler(enabled = showSurveyScreen) {
        showSurveyScreen = false
    }

    BackHandler(enabled = selectedCurriculumSubject != null) {
        selectedCurriculumSubject = null
    }

    BackHandler(enabled = selectedCurriculumClass != null && selectedCurriculumSubject == null) {
        selectedCurriculumClass = null
    }

    val profiles by repository.profiles.collectAsState()
    val activeProfileId by repository.activeProfileId.collectAsState()
    val activeProfile = repository.activeProfile

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = LiquidBackground,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            // Only show bottom navigation when not in the full-screen NotebookLM studio
            if (activeNotebookChapterId == null) {
                Surface(
                    color = LiquidSurfaceGlass,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LiquidCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .horizontalScroll(scrollState)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppDestination.entries.forEach { dest ->
                            val isSelected = currentDestination == dest
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        HapticHelper.triggerSelection(haptic)
                                        currentDestination = dest
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("nav_item_${dest.name.lowercase()}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) LiquidGlowIndigo.copy(alpha = 0.25f) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = if (isSelected) LiquidGlowCyan else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = dest.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activeNotebookChapterId != null) {
                NotebookLmScreen(
                    chapterId = activeNotebookChapterId!!,
                    repository = repository,
                    onBack = {
                        activeNotebookChapterId = null
                    }
                )
            } else {
                when (currentDestination) {
                    AppDestination.HOME -> HomeScreen(
                        profile = activeProfile,
                        onOpenProfileSwitcher = { showProfileSwitcher = true },
                        onNavigateToChapter = { chId -> activeNotebookChapterId = chId },
                        onNavigateToNotebookLm = { chId -> activeNotebookChapterId = chId },
                        onNavigateToTutor = { currentDestination = AppDestination.TUTOR },
                        onNavigateToVault = {
                            selectedCurriculumClass = activeProfile.cbseClass
                            currentDestination = AppDestination.VAULT
                        },
                        onNavigateToMastery = { currentDestination = AppDestination.MASTERY },
                        onNavigateToPlanner = { currentDestination = AppDestination.PLANNER },
                        repository = repository
                    )

                    AppDestination.MASTERY -> MasteryMapScreen(
                        repository = repository,
                        onNavigateToChapter = { chId -> activeNotebookChapterId = chId }
                    )

                    AppDestination.VAULT -> {
                        when {
                            selectedCurriculumSubject != null -> {
                                ChapterBrowserScreen(
                                    subject = selectedCurriculumSubject!!,
                                    repository = repository,
                                    onNavigateToChapterDetail = { chId -> activeNotebookChapterId = chId },
                                    onBack = { selectedCurriculumSubject = null }
                                )
                            }
                            selectedCurriculumClass != null -> {
                                SubjectSelectionScreen(
                                    selectedClass = selectedCurriculumClass!!,
                                    repository = repository,
                                    onSelectSubject = { subj -> selectedCurriculumSubject = subj },
                                    onBack = { selectedCurriculumClass = null }
                                )
                            }
                            else -> {
                                ClassSelectionScreen(
                                    repository = repository,
                                    onSelectClass = { cls -> selectedCurriculumClass = cls },
                                    onBack = { currentDestination = AppDestination.HOME }
                                )
                            }
                        }
                    }


                    AppDestination.PLANNER -> PlannerScreen(
                        repository = repository,
                        onNavigateToChapter = { chId -> activeNotebookChapterId = chId }
                    )

                    AppDestination.TUTOR -> FluidTutorScreen(
                        repository = repository,
                        onNavigateToChapter = { chId -> activeNotebookChapterId = chId }
                    )

                    AppDestination.ROOM -> StudyRoomScreen(
                        repository = repository,
                        onNavigateToChapter = { chId -> activeNotebookChapterId = chId }
                    )

                    AppDestination.PROFILE -> ProfileScreen(
                        repository = repository,
                        onOpenProfileSwitcher = { showProfileSwitcher = true },
                        onLaunchSurvey = { showSurveyScreen = true }
                    )
                }
            }

            if (showSurveyScreen) {
                StudentSurveyScreen(
                    repository = repository,
                    onSurveyCompleted = { showSurveyScreen = false },
                    onBack = { showSurveyScreen = false }
                )
            }
        }
    }


    if (showProfileSwitcher) {
        ProfileSwitcherSheet(
            profiles = profiles,
            activeProfileId = activeProfileId,
            onSelectProfile = { profId ->
                repository.switchProfile(profId)
            },
            onAddProfile = { name, email, cbseClass, stream, school, emoji, color ->
                repository.addProfile(name, email, cbseClass, stream, school, emoji, color)
            },
            onDismiss = { showProfileSwitcher = false }
        )
    }
}
