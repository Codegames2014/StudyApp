package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.remote.GeminiResponse
import com.example.data.remote.GeminiService
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

@Composable
fun CameraScannerSheet(
    studentName: String,
    cbseClass: Int,
    activeChapterTitle: String? = null,
    onSaveToNotes: ((title: String, solution: String, citation: String) -> Unit)? = null,
    onSendToTutor: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf<CameraSnapshotPreset?>(null) }
    var parsedSolution by remember { mutableStateOf<CameraDoubtResult?>(null) }

    // Toggle torch on camera hardware if available
    LaunchedEffect(isTorchOn, cameraInstance) {
        try {
            cameraInstance?.cameraControl?.enableTorch(isTorchOn)
        } catch (_: Throwable) {}
    }

    // Animated laser scan line
    val infiniteTransition = rememberInfiniteTransition(label = "laserScanner")
    val laserYPercent by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    val presets = listOf(
        CameraSnapshotPreset(
            id = "preset_quad",
            title = "Handwritten Quadratic Equation",
            previewSnippet = "2x² - 7x + 3 = 0. Find roots using discriminant.",
            subject = "Mathematics",
            category = "Algebra"
        ),
        CameraSnapshotPreset(
            id = "preset_circuit",
            title = "Ohm's Law Resistor Circuit",
            previewSnippet = "R₁ = 4Ω, R₂ = 6Ω in parallel with 12V battery.",
            subject = "Physics",
            category = "Current Electricity"
        ),
        CameraSnapshotPreset(
            id = "preset_redox",
            title = "Chemical Reaction in Textbook",
            previewSnippet = "CuO + H₂ -> Cu + H₂O. Identify reducing agent.",
            subject = "Chemistry",
            category = "Redox Reactions"
        ),
        CameraSnapshotPreset(
            id = "preset_bio",
            title = "Human Heart Double Circulation",
            previewSnippet = "Trace blood flow through pulmonary artery & aorta.",
            subject = "Biology",
            category = "Life Processes"
        )
    )

    fun sendToGeminiPipeline(queryText: String, imageBase64: String?, title: String, subject: String) {
        coroutineScope.launch {
            val response: GeminiResponse = GeminiService.askFluidTutor(
                studentQuery = queryText,
                studentName = studentName,
                cbseClass = cbseClass,
                activeChapterTitle = activeChapterTitle,
                imageBase64 = imageBase64
            )

            isCapturing = false
            parsedSolution = CameraDoubtResult(
                problemTitle = title,
                subject = subject,
                extractedQuery = queryText,
                socraticExplanation = response.text,
                citationBadges = response.citations
            )
        }
    }

    fun capturePhotoAndSolve() {
        HapticHelper.triggerImpact(haptic)
        isCapturing = true

        val capture = imageCaptureInstance
        if (hasCameraPermission && capture != null) {
            val executor = ContextCompat.getMainExecutor(context)
            capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val base64 = imageProxyToBase64(imageProxy)
                    imageProxy.close()

                    val query = selectedPreset?.previewSnippet
                        ?: "Transcribe this CBSE textbook equation/diagram and provide a step-by-step Socratic breakdown with NCERT citations."

                    val title = selectedPreset?.title ?: "Captured Textbook Doubt"
                    val subj = selectedPreset?.subject ?: "Science / Math"

                    sendToGeminiPipeline(query, base64, title, subj)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Fallback to text prompt
                    val query = selectedPreset?.previewSnippet ?: "Analyze scanned textbook diagram and explain with NCERT step-by-step reasoning."
                    sendToGeminiPipeline(query, null, selectedPreset?.title ?: "Scanned CBSE Problem", selectedPreset?.subject ?: "Science")
                }
            })
        } else {
            // Emulated / preset fallback
            val query = selectedPreset?.previewSnippet ?: "Analyze scanned textbook diagram and explain with NCERT step-by-step reasoning."
            sendToGeminiPipeline(query, null, selectedPreset?.title ?: "Scanned CBSE Problem", selectedPreset?.subject ?: "Science")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LiquidSurfaceElevated)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = null, tint = LiquidGlowCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CAMERAX DOUBT SCANNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowCyan
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticHelper.triggerSelection(haptic)
                            isTorchOn = !isTorchOn
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isTorchOn) LiquidGlowAmber else LiquidSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) Color.Black else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Viewfinder Frame with real CameraX PreviewView and animated laser scan line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(LiquidSurface.copy(alpha = 0.6f))
                        .border(1.5.dp, LiquidGlowIndigo.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Live CameraX Preview if permission granted
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }
                                        val imageCapture = ImageCapture.Builder()
                                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                            .build()
                                        imageCaptureInstance = imageCapture

                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.unbindAll()
                                        val cam = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageCapture
                                        )
                                        cameraInstance = cam
                                    } catch (_: Throwable) {
                                        // Ignore preview binding errors on emulators without camera
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Laser Line Overlay Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Corner view brackets
                        val bracketLen = 32.dp.toPx()
                        val stroke = 3.dp.toPx()
                        val bracketColor = LiquidGlowCyan

                        // Top-left
                        drawLine(bracketColor, Offset(0f, 0f), Offset(bracketLen, 0f), stroke)
                        drawLine(bracketColor, Offset(0f, 0f), Offset(0f, bracketLen), stroke)

                        // Top-right
                        drawLine(bracketColor, Offset(w, 0f), Offset(w - bracketLen, 0f), stroke)
                        drawLine(bracketColor, Offset(w, 0f), Offset(w, bracketLen), stroke)

                        // Bottom-left
                        drawLine(bracketColor, Offset(0f, h), Offset(bracketLen, h), stroke)
                        drawLine(bracketColor, Offset(0f, h), Offset(0f, h - bracketLen), stroke)

                        // Bottom-right
                        drawLine(bracketColor, Offset(w, h), Offset(w - bracketLen, h), stroke)
                        drawLine(bracketColor, Offset(w, h), Offset(w, h - bracketLen), stroke)

                        // Sweeping Laser
                        val laserY = h * laserYPercent
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    LiquidGlowCyan.copy(alpha = 0.5f),
                                    LiquidGlowCyan,
                                    LiquidGlowCyan.copy(alpha = 0.5f),
                                    Color.Transparent
                                ),
                                startY = laserY - 18f,
                                endY = laserY + 18f
                            ),
                            topLeft = Offset(8f, laserY - 8f),
                            size = Size(w - 16f, 16f)
                        )
                    }

                    // Status and Instruction overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = if (isCapturing) 0.6f else 0.25f))
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(color = LiquidGlowCyan, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Cross-referencing NCERT Vault & Gemini RAG...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FilterCenterFocus,
                                contentDescription = null,
                                tint = LiquidGlowCyan.copy(alpha = 0.85f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Center equation or diagram in frame",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = if (!hasCameraPermission) "Camera permission optional • Presets enabled" else "Live CameraX Feed Active",
                                fontSize = 10.sp,
                                color = if (hasCameraPermission) LiquidGlowEmerald else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick CBSE Snapshot Presets
                Text(
                    text = "Quick CBSE Snapshot Presets:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { preset ->
                        val isSelected = selectedPreset == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                                .border(1.dp, if (isSelected) LiquidGlowCyan else LiquidCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    selectedPreset = preset
                                    capturePhotoAndSolve()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = preset.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                                Text(
                                    text = preset.category,
                                    fontSize = 9.sp,
                                    color = if (isSelected) LiquidGlowCyan else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Camera Shutter Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(3.dp, LiquidGlowCyan, CircleShape)
                            .clickable {
                                capturePhotoAndSolve()
                            }
                            .testTag("btn_camera_shutter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            // --- Parsed Solution Sliding Sheet ---
            if (parsedSolution != null) {
                CameraParsedSolutionSheet(
                    result = parsedSolution!!,
                    onSaveNote = {
                        onSaveToNotes?.invoke(
                            parsedSolution!!.problemTitle,
                            parsedSolution!!.socraticExplanation,
                            parsedSolution!!.citationBadges.firstOrNull() ?: "NCERT Camera Scan"
                        )
                        HapticHelper.triggerImpact(haptic)
                    },
                    onFollowUp = {
                        onSendToTutor?.invoke("Can you provide another CBSE board practice problem similar to: ${parsedSolution!!.extractedQuery}")
                        onDismiss()
                    },
                    onClose = { parsedSolution = null }
                )
            }
        }
    }
}

// Convert CameraX ImageProxy to Base64 String for Gemini Multimodal API
fun imageProxyToBase64(imageProxy: ImageProxy): String? {
    return try {
        val plane = imageProxy.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    } catch (_: Throwable) {
        null
    }
}

@Composable
fun CameraParsedSolutionSheet(
    result: CameraDoubtResult,
    onSaveNote: () -> Unit,
    onFollowUp: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = LiquidSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, LiquidGlowCyan.copy(alpha = 0.4f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "SOLVED WITH NCERT RAG",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGlowEmerald
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "• ${result.subject}", fontSize = 11.sp, color = TextMuted)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = result.problemTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Grounded Citations
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    result.citationBadges.forEach { badge ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LiquidSurfaceElevated)
                                .border(1.dp, LiquidGlowIndigo.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = "🏷️ $badge", fontSize = 10.sp, color = LiquidGlowIndigo, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Solution Scrollable Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LiquidSurfaceElevated)
                        .padding(16.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        item {
                            Text(
                                text = "Problem Statement:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.extractedQuery,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = LiquidCardBorder)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Socratic Step-by-Step Breakdown:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGlowIndigo
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = result.socraticExplanation,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSaveNote,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to Notes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onFollowUp,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ask Follow-up", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class CameraSnapshotPreset(
    val id: String,
    val title: String,
    val previewSnippet: String,
    val subject: String,
    val category: String
)

data class CameraDoubtResult(
    val problemTitle: String,
    val subject: String,
    val extractedQuery: String,
    val socraticExplanation: String,
    val citationBadges: List<String>
)
