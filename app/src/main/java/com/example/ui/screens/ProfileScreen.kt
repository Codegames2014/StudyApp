package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyFluidRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.components.WeaknessRadarChart
import com.example.ui.components.XPMeter
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    repository: StudyFluidRepository,
    onOpenProfileSwitcher: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val activeProfile = repository.activeProfile
    val allProfiles by repository.profiles.collectAsState()

    var isEditingDetails by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(activeProfile.name) }
    var editClass by remember { mutableIntStateOf(activeProfile.cbseClass) }
    var editStream by remember { mutableStateOf(activeProfile.stream) }
    var editSchool by remember { mutableStateOf(activeProfile.schoolName) }

    LaunchedEffect(activeProfile.id) {
        editName = activeProfile.name
        editClass = activeProfile.cbseClass
        editStream = activeProfile.stream
        editSchool = activeProfile.schoolName
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth().testTag("profile_header_card"),
                glowAccentColor = Color(activeProfile.avatarColorHex)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(activeProfile.avatarColorHex).copy(alpha = 0.25f))
                            .border(2.dp, Color(activeProfile.avatarColorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = activeProfile.avatarEmoji, fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = activeProfile.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = activeProfile.email,
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(activeProfile.avatarColorHex).copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeProfile.displayClass,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(activeProfile.avatarColorHex)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🏫 ${activeProfile.schoolName}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Stats Matrix (Streak, Points, Bookmarks)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    glowAccentColor = LiquidGlowAmber
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activeProfile.studyStreakDays} Days",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowAmber
                        )
                        Text(text = "CBSE Streak", fontSize = 10.sp, color = TextMuted)
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    glowAccentColor = LiquidGlowIndigo
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⚡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activeProfile.knowledgePoints} XP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowIndigo
                        )
                        Text(text = "Knowledge XP", fontSize = 10.sp, color = TextMuted)
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    glowAccentColor = LiquidGlowCyan
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📑", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activeProfile.bookmarkedChapterIds.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlowCyan
                        )
                        Text(text = "Bookmarks", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        // Mastery Level & XP Progress Meter
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth().testTag("profile_xp_meter_card"),
                glowAccentColor = LiquidGlowAmber
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    XPMeter(
                        currentXp = activeProfile.knowledgePoints,
                        pointsPerLevel = 150,
                        modifier = Modifier.fillMaxWidth(),
                        barHeight = 10.dp,
                        showRankTitle = true
                    )
                }
            }
        }

        // Live Room Mistake Vault Weakness Radar Chart
        item {
            val liveMistakes by repository.getMistakesForActiveProfile().collectAsState(initial = emptyList())
            val allProfiles by repository.profiles.collectAsState()
            WeaknessRadarChart(
                mistakes = liveMistakes,
                studentName = activeProfile.name,
                cbseClass = activeProfile.cbseClass,
                profiles = allProfiles,
                activeProfileId = activeProfile.id,
                onSwitchProfile = { repository.switchProfile(it) }
            )
        }

        // Educational Profile Settings (CBSE Class & School)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Educational Configuration",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        TextButton(
                            onClick = {
                                if (isEditingDetails) {
                                    HapticHelper.triggerImpact(haptic)
                                    repository.updateActiveProfileDetails(
                                        name = editName,
                                        cbseClass = editClass,
                                        stream = editStream,
                                        schoolName = editSchool
                                    )
                                }
                                isEditingDetails = !isEditingDetails
                            }
                        ) {
                            Text(
                                text = if (isEditingDetails) "Save" else "Edit",
                                color = LiquidGlowCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isEditingDetails) {
                        ProfileInfoRow("Full Name", activeProfile.name)
                        ProfileInfoRow("CBSE Class", "Class ${activeProfile.cbseClass}")
                        if (activeProfile.cbseClass >= 11) {
                            ProfileInfoRow("Academic Stream", activeProfile.stream)
                        }
                        ProfileInfoRow("School / Institution", activeProfile.schoolName)
                        ProfileInfoRow("Google Account", activeProfile.email)
                    } else {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Student Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editSchool,
                            onValueChange = { editSchool = it },
                            label = { Text("School Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Multi-Account Switcher Hub Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Linked Student Accounts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Manage sibling & partner profiles on this device",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onOpenProfileSwitcher,
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Switch", fontSize = 12.sp)
                }
            }
        }

        items(allProfiles) { prof ->
            val isActive = prof.id == activeProfile.id
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (isActive) LiquidSurfaceElevated else LiquidSurfaceGlass,
                glowAccentColor = if (isActive) Color(prof.avatarColorHex) else null,
                onClick = {
                    if (!isActive) {
                        HapticHelper.triggerImpact(haptic)
                        repository.switchProfile(prof.id)
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(prof.avatarColorHex).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = prof.avatarEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prof.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${prof.displayClass} • ${prof.schoolName}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = "CURRENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidGlowEmerald)
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    HapticHelper.triggerImpact(haptic)
                    repository.signOutStudent()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_profile_signout"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidSurfaceElevated,
                    contentColor = LiquidGlowRose
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGlowRose.copy(alpha = 0.4f))
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Out", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Student Account", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
