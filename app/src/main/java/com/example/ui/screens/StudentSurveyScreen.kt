package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.remote.GeminiService
import com.example.data.remote.OnboardingRoadmapResult
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * MODULE 3: StudentSurveyScreen
 * Onboarding survey flow triggered on first launch or when adding/editing a profile in Account Settings:
 * - Inputs: Student Name, Class/Grade (Classes 1-12), Board Preference (CBSE / State Board), State & City selection (e.g. Bidar, Karnataka)
 * - AI Recommendation Pipeline: Feeds survey responses to Gemini to auto-generate personalized study roadmaps and NCERT chapter vaults.
 * - Storage: Store responses in StudentProfileEntity for isolated multi-account switching.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSurveyScreen(
    repository: StudyFluidRepository,
    onSurveyCompleted: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val activeProfile = repository.activeProfile

    var studentName by remember { mutableStateOf(activeProfile.name.ifBlank { "Aarav Sharma" }) }
    var selectedGrade by remember { mutableIntStateOf(activeProfile.cbseClass.coerceIn(1, 12)) }
    var selectedBoard by remember { mutableStateOf(activeProfile.board.ifBlank { "CBSE" }) }
    var selectedState by remember { mutableStateOf(activeProfile.state.ifBlank { "Karnataka" }) }
    var selectedCity by remember { mutableStateOf(activeProfile.city.ifBlank { "Bidar" }) }
    var selectedStream by remember { mutableStateOf(activeProfile.stream.ifBlank { "Science (PCM)" }) }
    var schoolName by remember { mutableStateOf(activeProfile.schoolName.ifBlank { "CBSE Affiliated Senior Secondary School" }) }

    // Stepper state: 0 = Demographics & Grade, 1 = Board & Location, 2 = AI Roadmap Preview & Finalize
    var currentStep by remember { mutableIntStateOf(0) }
    var isGeneratingAiRoadmap by remember { mutableStateOf(false) }
    var aiRoadmapResult by remember { mutableStateOf<OnboardingRoadmapResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val popularStates = listOf(
        "Karnataka", "Delhi", "Maharashtra", "Uttar Pradesh", "Telangana",
        "Tamil Nadu", "Rajasthan", "West Bengal", "Gujarat", "Kerala", "Punjab", "Bihar"
    )

    val popularCitiesByState = mapOf(
        "Karnataka" to listOf("Bidar", "Bengaluru", "Mysuru", "Hubballi", "Mangaluru", "Belagavi"),
        "Delhi" to listOf("New Delhi", "North Delhi", "South Delhi", "Dwarka"),
        "Maharashtra" to listOf("Pune", "Mumbai", "Nagpur", "Nashik", "Thane"),
        "Uttar Pradesh" to listOf("Lucknow", "Noida", "Varanasi", "Kanpur", "Agra"),
        "Telangana" to listOf("Hyderabad", "Warangal", "Nizamabad"),
        "Tamil Nadu" to listOf("Chennai", "Coimbatore", "Madurai"),
        "Rajasthan" to listOf("Jaipur", "Kota", "Jodhpur", "Udaipur")
    )

    val streams = listOf("Science (PCM)", "Science (PCB)", "Commerce", "Humanities", "General Foundation")

    fun triggerGenerateRoadmap() {
        HapticHelper.triggerImpact(haptic)
        isGeneratingAiRoadmap = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val roadmap = GeminiService.generatePersonalizedRoadmap(
                    studentName = studentName.trim(),
                    gradeClass = selectedGrade,
                    board = selectedBoard,
                    state = selectedState,
                    city = selectedCity,
                    stream = if (selectedGrade >= 11) selectedStream else "General"
                )
                aiRoadmapResult = roadmap
                isGeneratingAiRoadmap = false
                currentStep = 2
            } catch (e: Exception) {
                isGeneratingAiRoadmap = false
                errorMessage = "Failed to generate roadmap: ${e.message}"
            }
        }
    }

    fun finalizeAndSave() {
        HapticHelper.triggerImpact(haptic)
        val roadmap = aiRoadmapResult
        val summary = roadmap?.executiveSummary ?: "Personalized $selectedBoard Class $selectedGrade roadmap for $selectedCity, $selectedState."
        val recChapters = roadmap?.recommendedChapters ?: listOf("Chemical Reactions", "Optics", "Life Processes")
        val dailySchedule = roadmap?.dailySchedule?.joinToString(";") { "${it.timeSlot}: ${it.focusActivity}" } ?: ""

        repository.saveStudentSurvey(
            profileId = activeProfile.id,
            name = studentName.trim(),
            gradeClass = selectedGrade,
            board = selectedBoard,
            state = selectedState,
            city = selectedCity,
            stream = if (selectedGrade >= 11) selectedStream else "General",
            schoolName = schoolName.trim(),
            roadmapSummary = summary,
            recommendedChapters = recChapters,
            dailyScheduleJson = dailySchedule
        )

        onSurveyCompleted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("student_survey_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null || currentStep > 0) {
                        IconButton(
                            onClick = {
                                HapticHelper.triggerSelection(haptic)
                                if (currentStep > 0) currentStep-- else onBack?.invoke()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LiquidSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = LiquidGlowCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = "Student Onboarding & Survey",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Step ${currentStep + 1} of 3 • FlowCBSE Curriculum Engine",
                            fontSize = 11.sp,
                            color = LiquidGlowCyan
                        )
                    }
                }

                // Step indicator pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (idx == currentStep) 20.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (idx == currentStep) LiquidGlowIndigo
                                    else if (idx < currentStep) LiquidGlowCyan
                                    else Color.White.copy(alpha = 0.15f)
                                )
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    // STEP 1: Student Name, Grade & Stream
                    0 -> {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth(), glowAccentColor = LiquidGlowIndigo) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "1. Student Profile & Grade Level",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidGlowCyan
                                    )

                                    OutlinedTextField(
                                        value = studentName,
                                        onValueChange = { studentName = it },
                                        label = { Text("Student Full Name") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LiquidGlowCyan) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_survey_name"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LiquidGlowIndigo,
                                            unfocusedBorderColor = LiquidCardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    Column {
                                        Text(
                                            text = "Target Class / Grade (1 to 12)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items((1..12).toList()) { grade ->
                                                val isSelected = grade == selectedGrade
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) LiquidGlowCyan else LiquidCardBorder,
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable {
                                                            HapticHelper.triggerSelection(haptic)
                                                            selectedGrade = grade
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                                        .testTag("grade_chip_$grade")
                                                ) {
                                                    Text(
                                                        text = "Class $grade",
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Senior Secondary Stream (Class 11/12)
                                    if (selectedGrade >= 11) {
                                        Column {
                                            Text(
                                                text = "Senior Secondary Stream",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = LiquidGlowPink
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(streams) { stm ->
                                                    val isSelected = stm == selectedStream
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(if (isSelected) LiquidGlowPink.copy(alpha = 0.35f) else LiquidSurfaceElevated)
                                                            .border(
                                                                1.dp,
                                                                if (isSelected) LiquidGlowPink else LiquidCardBorder,
                                                                RoundedCornerShape(10.dp)
                                                            )
                                                            .clickable {
                                                                HapticHelper.triggerSelection(haptic)
                                                                selectedStream = stm
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = stm,
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color.White else TextSecondary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = schoolName,
                                        onValueChange = { schoolName = it },
                                        label = { Text("School Name (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = LiquidGlowCyan) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LiquidGlowIndigo,
                                            unfocusedBorderColor = LiquidCardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    HapticHelper.triggerSelection(haptic)
                                    currentStep = 1
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_survey_next_1"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
                            ) {
                                Text(text = "Next: Board & Location", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // STEP 2: Curriculum Board, State & City
                    1 -> {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth(), glowAccentColor = LiquidGlowPink) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "2. Educational Board & State Context",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidGlowCyan
                                    )

                                    // Board selection
                                    Column {
                                        Text(
                                            text = "Select Educational Board",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val boards = listOf("CBSE", "State Board", "ICSE")
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            boards.forEach { b ->
                                                val isSelected = b == selectedBoard
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) LiquidGlowCyan else LiquidCardBorder,
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable {
                                                            HapticHelper.triggerSelection(haptic)
                                                            selectedBoard = b
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = b,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // State selection
                                    Column {
                                        Text(
                                            text = "Select State / Union Territory",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(popularStates) { st ->
                                                val isSelected = st == selectedState
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) LiquidGlowCyan.copy(alpha = 0.25f) else LiquidSurfaceElevated)
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) LiquidGlowCyan else LiquidCardBorder,
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable {
                                                            HapticHelper.triggerSelection(haptic)
                                                            selectedState = st
                                                            val defaultCity = popularCitiesByState[st]?.firstOrNull() ?: selectedCity
                                                            selectedCity = defaultCity
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = st,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) LiquidGlowCyan else TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // City selection
                                    Column {
                                        Text(
                                            text = "Select or Enter City (e.g., Bidar, Bengaluru, Pune)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Quick city pills if state has predefined cities
                                        val cities = popularCitiesByState[selectedState] ?: emptyList()
                                        if (cities.isNotEmpty()) {
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            ) {
                                                items(cities) { c ->
                                                    val isSelected = c.equals(selectedCity, ignoreCase = true)
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) LiquidGlowIndigo.copy(alpha = 0.35f) else LiquidSurfaceElevated)
                                                            .border(
                                                                1.dp,
                                                                if (isSelected) LiquidGlowIndigo else LiquidCardBorder,
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable {
                                                                HapticHelper.triggerSelection(haptic)
                                                                selectedCity = c
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = c,
                                                            fontSize = 10.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) LiquidGlowCyan else TextMuted
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = selectedCity,
                                            onValueChange = { selectedCity = it },
                                            label = { Text("City / District") },
                                            leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = LiquidGlowCyan) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_survey_city"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = LiquidGlowIndigo,
                                                unfocusedBorderColor = LiquidCardBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (errorMessage != null) {
                            item {
                                Text(
                                    text = errorMessage!!,
                                    color = LiquidGlowRose,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = { triggerGenerateRoadmap() },
                                enabled = !isGeneratingAiRoadmap,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_survey_generate_ai"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
                            ) {
                                if (isGeneratingAiRoadmap) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "Synthesizing AI Study Roadmap...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Yellow)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Generate AI Study Roadmap ✨", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // STEP 3: AI Roadmap Presentation & Save
                    2 -> {
                        val roadmap = aiRoadmapResult
                        if (roadmap != null) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth(), glowAccentColor = LiquidGlowEmerald) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = "🗺️", fontSize = 20.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = roadmap.roadmapTitle,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = TextPrimary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (roadmap.isFromCloud) "Gemini Cloud" else "Local AI Engine",
                                                    fontSize = 9.sp,
                                                    color = LiquidGlowEmerald,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Text(
                                            text = roadmap.executiveSummary,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            lineHeight = 17.sp
                                        )

                                        HorizontalDivider(color = LiquidCardBorder)

                                        // Recommended High-Yield Chapter Vaults
                                        Text(
                                            text = "📚 Recommended High-Yield Chapter Vaults:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiquidGlowCyan
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            roadmap.recommendedChapters.forEachIndexed { idx, ch ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(LiquidSurfaceElevated)
                                                        .border(0.8.dp, LiquidCardBorder, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = "${idx + 1}.", fontSize = 11.sp, color = LiquidGlowPink, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(text = ch, fontSize = 11.sp, color = TextPrimary)
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = LiquidCardBorder)

                                        // Circadian Daily Revision Schedule
                                        Text(
                                            text = "⏰ Circadian Active Recall Schedule:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiquidGlowAmber
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            roadmap.dailySchedule.forEach { block ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(LiquidSurfaceElevated)
                                                        .padding(10.dp)
                                                ) {
                                                    Column {
                                                        Text(text = block.timeSlot, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                        Text(text = "• ${block.focusActivity}", fontSize = 11.sp, color = LiquidGlowCyan)
                                                        Text(text = "💡 ${block.pedagogyTip}", fontSize = 10.sp, color = TextMuted)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Button(
                                    onClick = { finalizeAndSave() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_survey_finalize_save"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LiquidGlowIndigo,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Save to Student Vault & Launch",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
