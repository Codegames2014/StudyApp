package com.example.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.data.model.*
import com.example.data.remote.GeminiService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbseExamSimulatorSheet(
    repository: StudyFluidRepository,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val activeProfile = repository.activeProfile

    val examPaper = remember { repository.getMockExamPaperForActiveProfile() }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val studentAnswers = remember { mutableStateMapOf<Int, String>() }

    // 3-hour exam countdown timer (180 minutes = 10,800 seconds)
    var secondsRemaining by remember { mutableIntStateOf(10800) }
    var isExamRunning by remember { mutableStateOf(true) }

    var isSubmittingAndGrading by remember { mutableStateOf(false) }
    var submissionResult by remember { mutableStateOf<ExamSubmissionResult?>(null) }
    var showSubmissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isExamRunning) {
        while (isExamRunning && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    val hours = secondsRemaining / 3600
    val minutes = (secondsRemaining % 3600) / 60
    val seconds = secondsRemaining % 60
    val formattedTimer = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val currentQ = examPaper.questions.getOrNull(currentQuestionIndex) ?: examPaper.questions.first()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF09090E),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.96f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Exam Hall Top Bar (Timer, Distraction-Free Hall Badge)
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
                        Text(text = "🏛️", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "CBSE Board Hall Simulator",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Distraction-Free 3-Hour Exam Mode",
                            fontSize = 11.sp,
                            color = LiquidGlowCyan
                        )
                    }
                }

                // Hall Timer Display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (secondsRemaining < 900) LiquidGlowRose.copy(alpha = 0.25f) else LiquidSurfaceElevated)
                        .border(1.dp, if (secondsRemaining < 900) LiquidGlowRose else LiquidCardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (secondsRemaining < 900) LiquidGlowRose else LiquidGlowCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTimer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (secondsRemaining < 900) LiquidGlowRose else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // If Graded, Show Post-Submission Scorecard & Step-Wise Rubric Breakdown
            val result = submissionResult
            if (result != null) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowAccentColor = LiquidGlowEmerald
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "CBSE BOARD OFFICIAL SCORECARD",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGlowCyan
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${result.totalScore.toInt()}",
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidGlowEmerald
                                    )
                                    Text(
                                        text = " / ${result.maxScore} MARKS",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                    )
                                }

                                Text(
                                    text = "Grade: ${result.grade} (${result.percentage.toInt()}%)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGlowAmber
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = result.generalExaminerFeedback,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Question-Wise Marking Scheme Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    items(result.evaluations) { eval ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowAccentColor = if (eval.marksAwarded == eval.maxMarks.toFloat()) LiquidGlowEmerald else LiquidGlowRose
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Question ${eval.questionNumber}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "+${eval.marksAwarded} / ${eval.maxMarks} MARKS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiquidGlowEmerald
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                eval.stepWiseFeedback.forEach { step ->
                                    Text(text = "• $step", fontSize = 11.sp, color = TextSecondary)
                                }

                                if (!eval.deductionReasons.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ Examiner Deduction: ${eval.deductionReasons}",
                                        fontSize = 11.sp,
                                        color = LiquidGlowRose
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Active Exam Hall View: Question Navigator, Question Content, Answer Box
                // Question Palette (1 to 39)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(examPaper.questions.size) { idx ->
                        val isSelected = idx == currentQuestionIndex
                        val hasAnswer = studentAnswers[examPaper.questions[idx].questionNumber]?.isNotBlank() == true
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) LiquidGlowIndigo
                                    else if (hasAnswer) LiquidGlowEmerald.copy(alpha = 0.25f)
                                    else LiquidSurfaceGlass
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) LiquidGlowCyan else if (hasAnswer) LiquidGlowEmerald else LiquidCardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    currentQuestionIndex = idx
                                }
                                .testTag("btn_exam_palette_$idx"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Q${examPaper.questions[idx].questionNumber}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Question Card
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowAccentColor = LiquidGlowIndigo
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "SECTION ${currentQ.section.sectionCode} • ${currentQ.marks} MARK${if (currentQ.marks > 1) "S" else ""}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiquidGlowCyan
                                        )
                                    }

                                    Text(
                                        text = currentQ.section.title,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Q${currentQ.questionNumber}. ${currentQ.questionText}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )

                                // If Section A MCQ: Show clickable options
                                if (currentQ.options.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    currentQ.options.forEachIndexed { optIdx, optText ->
                                        val isSelected = studentAnswers[currentQ.questionNumber] == optText
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) LiquidGlowIndigo.copy(alpha = 0.35f) else LiquidSurfaceElevated)
                                                .border(1.dp, if (isSelected) LiquidGlowCyan else LiquidCardBorder, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    HapticHelper.triggerImpact(haptic)
                                                    studentAnswers[currentQ.questionNumber] = optText
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = optText,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Student Handwritten / Typed Answer Sheet Box
                    item {
                        Text(
                            text = "Student Answer Sheet & Rough Derivations",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = studentAnswers[currentQ.questionNumber] ?: "",
                            onValueChange = { studentAnswers[currentQ.questionNumber] = it },
                            placeholder = { Text("Write your step-by-step formula derivation, SI units, and chemical equations here...", fontSize = 12.sp, color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LiquidSurfaceGlass)
                                .testTag("input_exam_answer"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LiquidGlowCyan,
                                unfocusedBorderColor = LiquidCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        if (currentQuestionIndex > 0) currentQuestionIndex--
                    },
                    enabled = currentQuestionIndex > 0,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidSurfaceElevated)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Prev", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Previous")
                }

                if (submissionResult == null) {
                    Button(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            isSubmittingAndGrading = true
                            coroutineScope.launch {
                                val result = GeminiService.gradeExamAnswers(
                                    paper = examPaper,
                                    studentAnswers = studentAnswers
                                )
                                isSubmittingAndGrading = false
                                isExamRunning = false
                                submissionResult = result
                                repository.incrementStreakAndXp(100)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowEmerald),
                        modifier = Modifier.testTag("btn_submit_exam_paper")
                    ) {
                        Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSubmittingAndGrading) "Grading Rubric..." else "Submit Paper (Grade with AI)")
                    }
                }

                Button(
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        if (currentQuestionIndex < examPaper.questions.size - 1) currentQuestionIndex++
                    },
                    enabled = currentQuestionIndex < examPaper.questions.size - 1,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidSurfaceElevated)
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
