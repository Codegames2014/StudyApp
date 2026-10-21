package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.remote.GeminiService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.*

enum class VoiceBuddyMode(val title: String, val subtitle: String, val emoji: String) {
    WALK_AND_LEARN("Walk & Learn", "Ambient hands-free summary while moving", "🚶‍♂️"),
    CHAPTER_QUIZZER("Chapter Quizzer", "Voice Q&A testing high-yield NCERT points", "🎯"),
    SOCRATIC_DEBATE("Socratic Challenge", "Deep-dive inquiry challenging your logic", "⚡")
}

data class SpokenExchange(
    val id: String,
    val speaker: String, // "You" or "StudyBuddy"
    val text: String,
    val isTutor: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceStudyBuddySheet(
    repository: StudyFluidRepository,
    chapterTitle: String? = "Chemical Reactions & Equations",
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeProfile = repository.activeProfile

    var currentMode by remember { mutableStateOf(VoiceBuddyMode.CHAPTER_QUIZZER) }
    var isMicActive by remember { mutableStateOf(true) }
    var isThinking by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var simulatedVolume by remember { mutableFloatStateOf(0.5f) }

    val transcripts = remember {
        mutableStateListOf(
            SpokenExchange(
                id = "init_1",
                speaker = "StudyBuddy",
                text = "Hey ${activeProfile.name}! StudyBuddy Live is connected. Let's practice ${chapterTitle ?: "Science"}. Ask me anything hands-free or say 'Quiz me'!",
                isTutor = true
            )
        )
    }

    val listState = rememberLazyListState()

    // TextToSpeech Engine
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                tts?.setSpeechRate(1.05f)
            }
        }
        textToSpeech = tts

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun speakText(text: String) {
        isSpeaking = true
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_buddy_${System.currentTimeMillis()}")
        coroutineScope.launch {
            // Simulate speaking audio modulation
            val durationMs = (text.length * 55).coerceIn(1200, 7000)
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < durationMs && isSpeaking) {
                simulatedVolume = 0.4f + (0.5f * sin((System.currentTimeMillis() / 80.0)).toFloat().absoluteValue)
                delay(60)
            }
            simulatedVolume = 0.2f
            isSpeaking = false
        }
    }

    fun sendVoiceTurn(userUtterance: String) {
        HapticHelper.triggerImpact(haptic)
        transcripts.add(SpokenExchange("usr_${System.currentTimeMillis()}", activeProfile.name, userUtterance, false))
        isThinking = true

        coroutineScope.launch {
            delay(350)
            val reply = GeminiService.askVoiceBuddy(
                spokenText = userUtterance,
                studentName = activeProfile.name,
                cbseClass = activeProfile.cbseClass,
                mode = currentMode.name,
                activeChapter = chapterTitle
            )
            isThinking = false
            transcripts.add(SpokenExchange("tut_${System.currentTimeMillis()}", "StudyBuddy", reply, true))
            speakText(reply)
            listState.animateScrollToItem(transcripts.size - 1)
        }
    }

    // Spring physics animators for the pulsating liquid orb
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val volumeSpring by animateFloatAsState(
        targetValue = simulatedVolume,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "volume_spring"
    )

    ModalBottomSheet(
        onDismissRequest = {
            textToSpeech?.stop()
            onDismiss()
        },
        containerColor = Color(0xFF09090D),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) LiquidGlowEmerald else if (isThinking) LiquidGlowCyan else LiquidGlowIndigo)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GEMINI MULTIMODAL LIVE AUDIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlowCyan
                    )
                }

                IconButton(
                    onClick = {
                        textToSpeech?.stop()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LiquidSurfaceElevated)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceBuddyMode.entries.forEach { mode ->
                    val isSelected = mode == currentMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) LiquidGlowIndigo.copy(alpha = 0.25f) else LiquidSurfaceGlass)
                            .border(1.dp, if (isSelected) LiquidGlowCyan else LiquidCardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                HapticHelper.triggerSelection(haptic)
                                currentMode = mode
                                sendVoiceTurn("Switch to ${mode.title} mode")
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = mode.emoji, fontSize = 16.sp)
                            Text(
                                text = mode.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- CENTRAL PULSATING LIQUID ORB ANIMATION ---
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val baseRadius = size.width * 0.32f

                    // Multi-layer deformed liquid organic paths
                    val numPoints = 64
                    for (layer in 0..2) {
                        val path = Path()
                        val layerScale = 1f + (layer * 0.15f * volumeSpring)
                        val layerPhase = pulsePhase + (layer * 1.2f)

                        for (i in 0 until numPoints) {
                            val angle = (i.toFloat() / numPoints) * 2f * PI.toFloat()
                            // Liquid deformation harmonics
                            val deformation = sin(angle * 3f + layerPhase) * 12f * volumeSpring +
                                    cos(angle * 5f - layerPhase * 0.8f) * 8f * volumeSpring

                            val r = (baseRadius * layerScale) + deformation
                            val x = cx + r * cos(angle)
                            val y = cy + r * sin(angle)

                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()

                        val layerBrush = Brush.radialGradient(
                            colors = when (layer) {
                                0 -> listOf(
                                    if (isSpeaking) LiquidGlowEmerald else LiquidGlowCyan,
                                    LiquidGlowIndigo,
                                    LiquidGlowIndigo.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                                1 -> listOf(
                                    LiquidGlowPink.copy(alpha = 0.5f),
                                    LiquidGlowIndigo.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                                else -> listOf(
                                    LiquidGlowCyan.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            },
                            center = Offset(cx, cy),
                            radius = size.width * 0.5f
                        )

                        drawPath(path, brush = layerBrush)
                        drawPath(path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1.5f))
                    }

                    // Center nucleus
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, LiquidGlowCyan, Color.Transparent),
                            center = Offset(cx, cy),
                            radius = baseRadius * 0.4f
                        ),
                        radius = baseRadius * 0.35f,
                        center = Offset(cx, cy)
                    )
                }

                // Center Icon / State
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else if (isThinking) Icons.Default.Psychology else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSpeaking) "Speaking..." else if (isThinking) "Thinking..." else "Listening...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Speech Prompts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "🎯 Quiz Me on Reactions",
                    "🚶 Walk Mode: Summarize Ch.1",
                    "💡 Formula Sign Tricks"
                ).forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LiquidSurfaceElevated)
                            .clickable {
                                sendVoiceTurn(prompt.substringAfter(" "))
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Transcript Feed
            Text(
                text = "Live Spoken Transcript",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiquidSurfaceGlass)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transcripts) { exchange ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (exchange.isTutor) Arrangement.Start else Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (exchange.isTutor) LiquidSurfaceElevated
                                    else LiquidGlowIndigo.copy(alpha = 0.35f)
                                )
                                .border(
                                    1.dp,
                                    if (exchange.isTutor) LiquidCardBorder else LiquidGlowCyan.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = exchange.speaker,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (exchange.isTutor) LiquidGlowCyan else LiquidGlowPink
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = exchange.text,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mic Toggle / Hands-Free Action
            Button(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    if (isSpeaking) {
                        textToSpeech?.stop()
                        isSpeaking = false
                    } else {
                        sendVoiceTurn("Explain double decomposition reaction")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_voice_buddy_mic"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpeaking) LiquidGlowRose else LiquidGlowIndigo
                )
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpeaking) "Interrupt / Stop Speaking" else "Tap to Speak (Hands-Free Active)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
