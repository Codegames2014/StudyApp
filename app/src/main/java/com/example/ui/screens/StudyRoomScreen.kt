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
import com.example.data.model.SiblingChatMessage
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*

@Composable
fun StudyRoomScreen(
    repository: StudyFluidRepository,
    onNavigateToChapter: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val profiles by repository.profiles.collectAsState()
    val room by repository.sharedStudyRoom.collectAsState()

    var scratchpadText by remember(room.sharedScratchpadText) {
        mutableStateOf(room.sharedScratchpadText)
    }
    var chatInput by remember { mutableStateOf("") }
    var isChatDrawerOpen by remember { mutableStateOf(false) }

    val quickStickers = listOf(
        "🔥 You got this!",
        "🏆 Board Exam Champion",
        "💡 Check this NCERT citation",
        "☕ Study break time!"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sibling Study Room",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Shared lounge for device profiles (Aarav & Diya)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                    .border(1.dp, LiquidGlowEmerald, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LIVE SYNC",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidGlowEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Floating Sibling Avatar Bubbles
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    profiles.take(3).forEachIndexed { i, prof ->
                        Box(
                            modifier = Modifier
                                .offset(x = (-8 * i).dp)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(prof.avatarColorHex).copy(alpha = 0.3f))
                                .border(2.dp, Color(prof.avatarColorHex), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = prof.avatarEmoji, fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = "Co-Studying Together",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${profiles.size} active student profiles",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Chat Drawer toggle
                IconButton(
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        isChatDrawerOpen = !isChatDrawerOpen
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LiquidGlowIndigo.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = if (isChatDrawerOpen) Icons.Default.KeyboardArrowUp else Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = LiquidGlowCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simultaneous AI Podcast Co-Listening Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowAccentColor = if (room.isCoListeningPodcast) LiquidGlowCyan else null,
            onClick = {
                HapticHelper.triggerImpact(haptic)
                repository.toggleCoListening()
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (room.isCoListeningPodcast) LiquidGlowCyan else LiquidSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (room.isCoListeningPodcast) Icons.Default.Headphones else Icons.Default.HeadphonesBattery,
                            contentDescription = null,
                            tint = if (room.isCoListeningPodcast) Color.Black else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (room.isCoListeningPodcast) "Synchronized Co-Listening Active" else "Co-Listen to Deep Dive Podcast",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (room.isCoListeningPodcast) LiquidGlowCyan else TextPrimary
                        )
                        Text(
                            text = room.activeChapterTitle,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = if (room.isCoListeningPodcast) "PLAYING" else "TAP TO JOIN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (room.isCoListeningPodcast) LiquidGlowEmerald else LiquidGlowCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Shared Scratchpad Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Real-Time Shared Scratchpad",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Auto-Synced",
                fontSize = 11.sp,
                color = LiquidGlowEmerald
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = scratchpadText,
            onValueChange = {
                scratchpadText = it
                repository.updateSharedScratchpad(it)
            },
            placeholder = { Text("Write shared notes, formulas, or homework reminders between siblings...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("input_shared_scratchpad"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LiquidGlowIndigo,
                unfocusedBorderColor = LiquidCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = LiquidSurfaceElevated,
                unfocusedContainerColor = LiquidSurfaceElevated
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Slide-in Sibling Live Chat Drawer
        AnimatedVisibility(visible = isChatDrawerOpen) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Sibling Motivational Chat",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlowCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(room.siblingMessages) { msg ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${msg.senderEmoji} ${msg.senderName}: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidGlowIndigo)
                                Text(text = msg.text, fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Stickers
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickStickers) { sticker ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiquidSurfaceElevated)
                                    .clickable {
                                        HapticHelper.triggerImpact(haptic)
                                        repository.sendSiblingMessage(sticker, isSticker = true)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = sticker, fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Message sibling...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    HapticHelper.triggerSelection(haptic)
                                    repository.sendSiblingMessage(chatInput)
                                    chatInput = ""
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = LiquidGlowIndigo)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}
