package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.model.HandwrittenNoteScanResult
import com.example.data.model.MindMapNode
import com.example.data.model.OcrFlashcard
import com.example.data.remote.GeminiService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandwrittenNotesConverterSheet(
    repository: StudyFluidRepository,
    defaultSubject: String = "Chemistry",
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var extractionResult by remember { mutableStateOf<HandwrittenNoteScanResult?>(null) }
    var selectedNotePreset by remember { mutableStateOf("Chemical Reactions Notes") }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Flashcards, 1 = Mind-Map, 2 = Raw Notes
    var flippedCardId by remember { mutableStateOf<String?>(null) }
    var isSavedToVault by remember { mutableStateOf(false) }

    // Scanning laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_scan")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    fun startOcrExtraction(notesText: String, subject: String) {
        HapticHelper.triggerImpact(haptic)
        isProcessing = true
        isSavedToVault = false

        coroutineScope.launch {
            delay(600)
            val result = GeminiService.extractFlashcardsAndMindMap(
                noteTextOrContext = notesText,
                subject = subject
            )
            isProcessing = false
            extractionResult = result
        }
    }

    LaunchedEffect(Unit) {
        startOcrExtraction("Classroom handwritten notes: Lavoisier Law of Conservation of mass, balanced equations, 2Pb(NO3)2 heating gives yellow PbO and brown NO2 fumes.", defaultSubject)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0C0C12),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.94f)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LiquidGlowIndigo.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📝", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Handwritten Notes ➔ Flashcards",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Gemini OCR + Mind-Map Extraction Engine",
                            fontSize = 11.sp,
                            color = LiquidGlowCyan
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

            // Sample Notes Source Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "⚗️ Chemistry Reactions",
                    "🔭 Physics Lens Derivations",
                    "📐 Maths Calculus Formulas"
                ).forEach { preset ->
                    val isSelected = selectedNotePreset == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) LiquidGlowIndigo.copy(alpha = 0.3f) else LiquidSurfaceGlass)
                            .border(1.dp, if (isSelected) LiquidGlowCyan else LiquidCardBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                HapticHelper.triggerSelection(haptic)
                                selectedNotePreset = preset
                                val subj = if (preset.contains("Chemistry")) "Chemistry" else if (preset.contains("Physics")) "Physics" else "Mathematics"
                                startOcrExtraction("Handwritten notebook scan for $preset", subj)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.substringAfter(" "),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Viewfinder / Laser Scanning Simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF14141E))
                    .border(1.dp, LiquidGlowIndigo.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (isProcessing) {
                        val y = size.height * laserY
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, LiquidGlowCyan, LiquidGlowIndigo, Color.Transparent)
                            ),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2Pb(NO₃)₂\n➔ 2PbO\n+ 4NO₂\n+ O₂",
                            fontSize = 9.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isProcessing) "Gemini OCR Parsing Handwritten Script..." else "Extraction Complete: 3 Flashcards & Concept Mind-Map",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProcessing) LiquidGlowCyan else LiquidGlowEmerald
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Parsed chemical equations, definitions, and CBSE board weightage tags.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Result Tabs: Flashcards vs Mind-Map
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = LiquidGlowCyan,
                divider = {}
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        activeTab = 0
                    },
                    text = { Text("⚡ Digital Flashcards", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        activeTab = 1
                    },
                    text = { Text("🧠 Mind-Map Tree", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            val res = extractionResult
            if (res != null) {
                if (activeTab == 0) {
                    // Flashcards List with 3D Flip
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(res.flashcards) { card ->
                            val isFlipped = flippedCardId == card.id
                            val flipRotation by animateFloatAsState(
                                targetValue = if (isFlipped) 180f else 0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "card_flip"
                            )

                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        rotationY = flipRotation
                                        cameraDistance = 12f * density
                                    }
                                    .clickable {
                                        HapticHelper.triggerImpact(haptic)
                                        flippedCardId = if (isFlipped) null else card.id
                                    },
                                glowAccentColor = if (card.difficulty == "Board High-Yield") LiquidGlowAmber else LiquidGlowIndigo
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .graphicsLayer {
                                            if (flipRotation > 90f) rotationY = 180f
                                        }
                                ) {
                                    if (flipRotation <= 90f) {
                                        // FRONT: Question
                                        Column {
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
                                                    Text(text = "QUESTION (TAP TO FLIP)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidGlowCyan)
                                                }

                                                Text(text = card.difficulty, fontSize = 10.sp, color = LiquidGlowAmber, fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = card.frontQuestion,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = "📌 ${card.ncertCitation}", fontSize = 10.sp, color = TextMuted)
                                        }
                                    } else {
                                        // BACK: Answer
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(text = "ANSWER & FORMULA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidGlowEmerald)
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = card.backAnswer,
                                                fontSize = 13.sp,
                                                color = TextSecondary,
                                                lineHeight = 18.sp
                                            )

                                            if (!card.formulaOrEquation.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF0F172A))
                                                        .padding(8.dp)
                                                ) {
                                                    Text(
                                                        text = card.formulaOrEquation,
                                                        fontSize = 12.sp,
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
                    }
                } else {
                    // Mind-Map Tree Visualization
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            MindMapNodeView(res.mindMapRoot, depth = 0)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save to Room Vault Button
            Button(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    coroutineScope.launch {
                        val activeProfileId = repository.activeProfile.id
                        repository.addRoomVaultItem(
                            vaultId = "vault_aarav_chem",
                            title = "OCR Extracted: ${res?.scanTitle ?: "Class Notes"}",
                            content = res?.flashcards?.joinToString("\n\n") { "Q: ${it.frontQuestion}\nA: ${it.backAnswer}" } ?: "",
                            itemType = "FLASHCARD",
                            citationTag = "NCERT Handwritten Extraction"
                        )
                        isSavedToVault = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_flashcards_vault"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSavedToVault) LiquidGlowEmerald else LiquidGlowIndigo
                )
            ) {
                Icon(
                    imageVector = if (isSavedToVault) Icons.Default.CheckCircle else Icons.Default.BookmarkAdd,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSavedToVault) "Saved to Room Vault! 🎉" else "Save Flashcards to Study Vault",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MindMapNodeView(node: MindMapNode, depth: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 14).dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(node.colorHex).copy(alpha = 0.15f))
                .border(1.dp, Color(node.colorHex).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(node.colorHex))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = node.title,
                    fontSize = if (depth == 0) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (!node.subtitle.isNullOrBlank()) {
                    Text(
                        text = node.subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        if (node.children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            node.children.forEach { child ->
                MindMapNodeView(node = child, depth = depth + 1)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
