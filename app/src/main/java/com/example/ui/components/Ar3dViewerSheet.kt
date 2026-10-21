package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.remote.GeminiService
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ar3dViewerSheet(
    initialModel: Ar3dModel,
    chapterTitle: String = "Chemical Reactions & Equations",
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var activeModel by remember { mutableStateOf(initialModel) }
    var rotX by remember { mutableFloatStateOf(25f) }
    var rotY by remember { mutableFloatStateOf(35f) }
    var scale by remember { mutableFloatStateOf(1.0f) }
    var isAutoSpinning by remember { mutableStateOf(true) }
    var isExplodedView by remember { mutableStateOf(false) }
    var isWireframe by remember { mutableStateOf(false) }
    var showInfoOverlay by remember { mutableStateOf(true) }
    var customQueryPrompt by remember { mutableStateOf("") }
    var isGeneratingAiModel by remember { mutableStateOf(false) }

    val explodedAnim by animateFloatAsState(
        targetValue = if (isExplodedView) 1.6f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "exploded_spring"
    )

    // Auto-spin animation
    LaunchedEffect(isAutoSpinning) {
        if (isAutoSpinning) {
            while (true) {
                withFrameNanos {
                    rotY = (rotY + 0.6f) % 360f
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D0D12),
        scrimColor = Color.Black.copy(alpha = 0.85f),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF05050A)
                        )
                    )
                )
        ) {
            // AR Reticle Background / 3D Holographic Plane Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color(0x1A38BDF8)
                val step = 40.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
                }
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
                }
                // Center reticle
                drawCircle(
                    color = Color(0x3338BDF8),
                    radius = 160.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // 3D Canvas Projection Viewport
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            isAutoSpinning = false
                            rotY += dragAmount.x * 0.7f
                            rotX = (rotX - dragAmount.y * 0.7f).coerceIn(-85f, 85f)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val baseScale = min(size.width, size.height) * 0.42f * scale

                    val radX = Math.toRadians(rotX.toDouble())
                    val radY = Math.toRadians(rotY.toDouble())

                    val cosX = cos(radX).toFloat()
                    val sinX = sin(radX).toFloat()
                    val cosY = cos(radY).toFloat()
                    val sinY = sin(radY).toFloat()

                    // Project nodes to 3D camera coordinates
                    data class ProjectedNode(
                        val node: ArNode3D,
                        val screenX: Float,
                        val screenY: Float,
                        val depthZ: Float,
                        val radiusPx: Float
                    )

                    val projectedNodes = activeModel.nodes.map { node ->
                        val ex = node.x * explodedAnim
                        val ey = node.y * explodedAnim
                        val ez = node.z * explodedAnim

                        // Rotate Y
                        val x1 = ex * cosY + ez * sinY
                        val y1 = ey
                        val z1 = -ex * sinY + ez * cosY

                        // Rotate X
                        val x2 = x1
                        val y2 = y1 * cosX - z1 * sinX
                        val z2 = y1 * sinX + z1 * cosX

                        // Perspective projection
                        val fov = 3.5f
                        val cameraZ = z2 + fov
                        val pFactor = if (cameraZ > 0.1f) fov / cameraZ else 1f

                        val sx = cx + x2 * baseScale * pFactor
                        val sy = cy - y2 * baseScale * pFactor
                        val rPx = (node.radius * baseScale * pFactor).coerceAtLeast(14f)

                        ProjectedNode(node, sx, sy, z2, rPx)
                    }

                    val nodeMap = projectedNodes.associateBy { it.node.id }

                    // Draw bonds (cylinders / lines) sorted by midpoint depth
                    activeModel.bonds.sortedBy { bond ->
                        val s = nodeMap[bond.sourceNodeId]?.depthZ ?: 0f
                        val t = nodeMap[bond.targetNodeId]?.depthZ ?: 0f
                        (s + t) / 2f
                    }.forEach { bond ->
                        val s = nodeMap[bond.sourceNodeId] ?: return@forEach
                        val t = nodeMap[bond.targetNodeId] ?: return@forEach

                        val bondColor = Color(bond.colorHex)
                        if (bond.bondOrder == 2) {
                            // Double bond: two parallel lines
                            val dx = t.screenX - s.screenX
                            val dy = t.screenY - s.screenY
                            val len = hypot(dx, dy)
                            if (len > 0f) {
                                val nx = -dy / len * 4f
                                val ny = dx / len * 4f
                                drawLine(
                                    color = bondColor.copy(alpha = 0.85f),
                                    start = Offset(s.screenX + nx, s.screenY + ny),
                                    end = Offset(t.screenX + nx, t.screenY + ny),
                                    strokeWidth = 3f
                                )
                                drawLine(
                                    color = bondColor.copy(alpha = 0.85f),
                                    start = Offset(s.screenX - nx, s.screenY - ny),
                                    end = Offset(t.screenX - nx, t.screenY - ny),
                                    strokeWidth = 3f
                                )
                            }
                        } else {
                            drawLine(
                                color = bondColor.copy(alpha = 0.85f),
                                start = Offset(s.screenX, s.screenY),
                                end = Offset(t.screenX, t.screenY),
                                strokeWidth = if (bond.bondOrder == 3) 6f else 4f
                            )
                        }
                    }

                    // Draw nodes sorted back-to-front (depth sorting)
                    projectedNodes.sortedBy { it.depthZ }.forEach { pn ->
                        val baseColor = Color(pn.node.colorHex)

                        if (isWireframe) {
                            drawCircle(
                                color = baseColor,
                                radius = pn.radiusPx,
                                center = Offset(pn.screenX, pn.screenY),
                                style = Stroke(width = 2.5f)
                            )
                        } else {
                            // 3D Sphere shading with radial gradient highlight
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        baseColor,
                                        baseColor.copy(alpha = 0.7f),
                                        Color.Black.copy(alpha = 0.9f)
                                    ),
                                    center = Offset(pn.screenX - pn.radiusPx * 0.35f, pn.screenY - pn.radiusPx * 0.35f),
                                    radius = pn.radiusPx * 1.2f
                                ),
                                radius = pn.radiusPx,
                                center = Offset(pn.screenX, pn.screenY)
                            )
                        }

                        // Outer glow border
                        drawCircle(
                            color = baseColor.copy(alpha = 0.4f),
                            radius = pn.radiusPx + 2f,
                            center = Offset(pn.screenX, pn.screenY),
                            style = Stroke(width = 1.2f)
                        )
                    }
                }
            }

            // Top Header: Model Title, AR Tag, Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LiquidGlowCyan.copy(alpha = 0.2f))
                            .border(1.dp, LiquidGlowCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AR 3D DESK VIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = activeModel.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = activeModel.formulaOrSymbol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = LiquidGlowIndigo
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LiquidSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close AR",
                        tint = TextPrimary
                    )
                }
            }

            // Bottom Controls Bar (Glassmorphic)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info Card (Collapsible)
                AnimatedVisibility(visible = showInfoOverlay) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowAccentColor = LiquidGlowIndigo
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NCERT Scientific Principle",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGlowCyan
                                )
                                Text(
                                    text = activeModel.ncertCitation,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = activeModel.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "💡", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeModel.funFact,
                                    fontSize = 11.sp,
                                    color = LiquidGlowAmber,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Quick Model Selector Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArModelRegistry.allModels.forEach { model ->
                        val isSelected = model.id == activeModel.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceGlass)
                                .border(1.dp, if (isSelected) LiquidGlowCyan else LiquidCardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    HapticHelper.triggerSelection(haptic)
                                    activeModel = model
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = model.formulaOrSymbol.take(8),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Action Overlays: Auto-spin, Explode, Wireframe, Zoom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left buttons: Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Auto-spin toggle
                        FilterChip(
                            selected = isAutoSpinning,
                            onClick = {
                                HapticHelper.triggerImpact(haptic)
                                isAutoSpinning = !isAutoSpinning
                            },
                            label = { Text(if (isAutoSpinning) "Spinning" else "Pause", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isAutoSpinning) Icons.Default.RotateRight else Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LiquidGlowIndigo.copy(alpha = 0.3f),
                                selectedLabelColor = LiquidGlowCyan
                            )
                        )

                        // Explode View
                        FilterChip(
                            selected = isExplodedView,
                            onClick = {
                                HapticHelper.triggerImpact(haptic)
                                isExplodedView = !isExplodedView
                            },
                            label = { Text("Explode", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LiquidGlowRose.copy(alpha = 0.3f),
                                selectedLabelColor = LiquidGlowRose
                            )
                        )

                        // Wireframe
                        FilterChip(
                            selected = isWireframe,
                            onClick = {
                                HapticHelper.triggerImpact(haptic)
                                isWireframe = !isWireframe
                            },
                            label = { Text("Wire", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LiquidGlowEmerald.copy(alpha = 0.3f),
                                selectedLabelColor = LiquidGlowEmerald
                            )
                        )
                    }

                    // Zoom / Reset
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                HapticHelper.triggerImpact(haptic)
                                scale = (scale + 0.15f).coerceAtMost(2.0f)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LiquidSurfaceElevated)
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                HapticHelper.triggerImpact(haptic)
                                scale = (scale - 0.15f).coerceAtLeast(0.6f)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LiquidSurfaceElevated)
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                HapticHelper.triggerImpact(haptic)
                                rotX = 25f
                                rotY = 35f
                                scale = 1.0f
                                isAutoSpinning = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LiquidSurfaceElevated)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
