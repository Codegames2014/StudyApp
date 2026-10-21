package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.TutorLanguageTone
import com.example.data.remote.GeminiService
import com.example.ui.components.CameraScannerSheet
import com.example.ui.components.CbseExamSimulatorSheet
import com.example.ui.components.GlassCard
import com.example.ui.components.HandwrittenNotesConverterSheet
import com.example.ui.components.HapticHelper
import com.example.ui.components.VoiceStudyBuddySheet
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FluidTutorScreen(
    repository: StudyFluidRepository,
    onNavigateToChapter: ((String) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val activeProfile = repository.activeProfile

    val allChats by repository.profileChats.collectAsState()
    val messages = allChats[activeProfile.id] ?: emptyList()

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var attachedDiagram by remember { mutableStateOf<String?>(null) }
    var showCameraScanner by remember { mutableStateOf(false) }
    var showVoiceBuddy by remember { mutableStateOf(false) }
    var showNotesConverter by remember { mutableStateOf(false) }
    var showExamSimulator by remember { mutableStateOf(false) }

    val currentTone by repository.activeTutorTone.collectAsState()

    val listState = rememberLazyListState()

    val quickQuestions = listOf(
        "Explain Redox reaction with NCERT example",
        "Why is Double Circulation necessary in humans?",
        "Derive Lens formula sign convention",
        "How to find nature of roots from Discriminant D?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(query: String, diagramName: String? = null) {
        if (query.isBlank() && diagramName == null) return
        val textToSend = if (diagramName != null && query.isBlank()) {
            "Please analyze this CBSE textbook diagram: $diagramName and explain step-by-step."
        } else query

        HapticHelper.triggerImpact(haptic)

        val userMsg = ChatMessage(
            id = "usr_${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = textToSend,
            attachedDiagramName = diagramName
        )
        repository.addChatMessage(activeProfile.id, userMsg)
        inputText = ""
        attachedDiagram = null
        isThinking = true

        coroutineScope.launch {
            val response = GeminiService.askFluidTutor(
                studentQuery = textToSend,
                studentName = activeProfile.name,
                cbseClass = activeProfile.cbseClass,
                tone = currentTone
            )

            val tutorMsg = ChatMessage(
                id = "tutor_${System.currentTimeMillis()}",
                sender = MessageSender.FLUID_TUTOR,
                text = response.text,
                citationBadges = response.citations
            )
            repository.addChatMessage(activeProfile.id, tutorMsg)
            repository.incrementStreakAndXp(20)
            isThinking = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LiquidBackground)
        ) {
            // --- Tutor Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                            .border(1.dp, LiquidGlowIndigo, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧘", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FluidTutor AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ONLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGlowEmerald
                                )
                            }
                        }

                        Text(
                            text = "Socratic CBSE Tutor for ${activeProfile.name} (${activeProfile.displayClass})",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Voice Study Buddy (Feature 2)
                    IconButton(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            showVoiceBuddy = true
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                            .testTag("btn_top_voice_buddy")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Study Buddy",
                            tint = LiquidGlowCyan
                        )
                    }

                    // Notes-to-Flashcards OCR (Feature 3)
                    IconButton(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            showNotesConverter = true
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LiquidGlowPink.copy(alpha = 0.25f))
                            .testTag("btn_top_notes_ocr")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Notes OCR",
                            tint = LiquidGlowPink
                        )
                    }

                    // CBSE Exam Simulator (Feature 5)
                    IconButton(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            showExamSimulator = true
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LiquidGlowEmerald.copy(alpha = 0.25f))
                            .testTag("btn_top_exam_simulator")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = "Exam Simulator",
                            tint = LiquidGlowEmerald
                        )
                    }

                    // Camera Doubt Scanner
                    IconButton(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            showCameraScanner = true
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LiquidGlowCyan.copy(alpha = 0.2f))
                            .testTag("btn_top_camera_scanner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Camera Scanner",
                            tint = LiquidGlowCyan
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticHelper.triggerSelection(haptic)
                            repository.clearChat(activeProfile.id)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear Chat",
                            tint = TextMuted
                        )
                    }
                }
            }

            // --- FEATURE 6: MULTI-LANGUAGE REGIONAL SUPPORT & TONE PILLS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TutorLanguageTone.entries.forEach { toneOption ->
                    val isSelected = toneOption == currentTone
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) LiquidGlowIndigo.copy(alpha = 0.35f) else LiquidSurfaceGlass)
                            .border(1.dp, if (isSelected) LiquidGlowCyan else LiquidCardBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                HapticHelper.triggerSelection(haptic)
                                repository.setTutorTone(toneOption)
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = toneOption.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = toneOption.title.substringBefore(" "),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LiquidGlowCyan else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // --- Quick Question Suggestion Pills ---
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickQuestions) { qq ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(LiquidSurfaceElevated)
                            .border(1.dp, LiquidCardBorder, RoundedCornerShape(16.dp))
                            .clickable { sendMessage(qq) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = qq, fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            // --- Chat Thread ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(message = msg)
                }

                if (isThinking) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = LiquidGlowIndigo
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "FluidTutor is analyzing NCERT textbooks & reasoning...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // --- Active Diagram Attachment Preview ---
            if (attachedDiagram != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LiquidGlowIndigo.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = LiquidGlowIndigo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Diagram Attached: $attachedDiagram", fontSize = 11.sp, color = TextPrimary)
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { attachedDiagram = null }
                    )
                }
            }

            // --- Input Dock ---
            Surface(
                color = LiquidSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 76.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            showCameraScanner = true
                        },
                        modifier = Modifier.testTag("btn_attach_diagram")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera Doubt Scanner",
                            tint = LiquidGlowCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask doubt or CBSE question...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_tutor_message"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidGlowIndigo,
                            unfocusedBorderColor = LiquidCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = LiquidSurfaceElevated,
                            unfocusedContainerColor = LiquidSurfaceElevated
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            sendMessage(inputText, attachedDiagram)
                        },
                        enabled = inputText.isNotBlank() || attachedDiagram != null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() || attachedDiagram != null) LiquidGlowIndigo else LiquidSurfaceElevated)
                            .testTag("btn_send_tutor_message")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() || attachedDiagram != null) Color.White else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Camera Scanner Modal Overlay
        if (showCameraScanner) {
            CameraScannerSheet(
                studentName = activeProfile.name,
                cbseClass = activeProfile.cbseClass,
                onSaveToNotes = { title, solution, citation ->
                    repository.addNote(
                        chapterId = "cl10_sci_ch1",
                        title = title,
                        content = solution,
                        citationTag = citation
                    )
                },
                onSendToTutor = { followUpQuery ->
                    sendMessage(followUpQuery)
                },
                onDismiss = { showCameraScanner = false }
            )
        }

        // Feature 2: Voice Study Buddy (Gemini Live Ambient Audio)
        if (showVoiceBuddy) {
            VoiceStudyBuddySheet(
                repository = repository,
                chapterTitle = "Chemical Reactions & Equations",
                onDismiss = { showVoiceBuddy = false }
            )
        }

        // Feature 3: Smart Handwritten Notes-to-Flashcards Converter
        if (showNotesConverter) {
            HandwrittenNotesConverterSheet(
                repository = repository,
                defaultSubject = "Chemistry",
                onDismiss = { showNotesConverter = false }
            )
        }

        // Feature 5: CBSE Exam Board Paper Simulator & Hall Timer
        if (showExamSimulator) {
            CbseExamSimulatorSheet(
                repository = repository,
                onDismiss = { showExamSimulator = false }
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LiquidGlowIndigo.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🧘", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Attached diagram badge if any
            if (message.attachedDiagramName != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LiquidGlowIndigo.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📐 ${message.attachedDiagramName}",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Message Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) LiquidGlowIndigo else LiquidSurfaceElevated)
                    .border(
                        1.dp,
                        if (isUser) LiquidGlowIndigo else LiquidCardBorder,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else TextPrimary,
                    lineHeight = 18.sp
                )
            }

            // Citations
            if (message.citationBadges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    message.citationBadges.forEach { badge ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LiquidSurfaceElevated)
                                .border(1.dp, LiquidGlowCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🏷️ $badge",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowCyan
                            )
                        }
                    }
                }
            }
        }
    }
}
