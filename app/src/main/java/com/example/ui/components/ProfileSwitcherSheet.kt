package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
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
import com.example.data.model.StudentProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitcherSheet(
    profiles: List<StudentProfile>,
    activeProfileId: String,
    onSelectProfile: (String) -> Unit,
    onAddProfile: (
        name: String,
        email: String,
        cbseClass: Int,
        stream: String,
        schoolName: String,
        avatarEmoji: String,
        colorHex: Long
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isAddingNewProfile by remember { mutableStateOf(false) }

    // Form fields for adding new profile
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newClass by remember { mutableIntStateOf(10) }
    var newStream by remember { mutableStateOf("Science (PCM)") }
    var newSchool by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🎓") }
    var selectedColor by remember { mutableLongStateOf(0xFF6366F1) }

    val emojis = listOf("🎓", "🚀", "💡", "🔬", "📚", "✨", "🪐", "⚡")
    val colors = listOf(0xFF6366F1, 0xFF06B6D4, 0xFF10B981, 0xFF8B5CF6, 0xFFF59E0B, 0xFFEC4899)
    val streams = listOf("Science (PCM)", "Science (PCB)", "Commerce", "Humanities", "General Foundation")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LiquidSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(LiquidGlowIndigo.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAddingNewProfile) "Add Student Profile" else "Switch Student Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isAddingNewProfile) "Link sibling or study partner account" else "Isolated vaults & CBSE curriculum per student",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        if (isAddingNewProfile) {
                            isAddingNewProfile = false
                        } else {
                            onDismiss()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = isAddingNewProfile,
                transitionSpec = {
                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "profileSheetContent"
            ) { addingMode ->
                if (!addingMode) {
                    // Profile Switcher List
                    Column {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.heightIn(max = 340.dp)
                        ) {
                            items(profiles, key = { it.id }) { profile ->
                                val isActive = profile.id == activeProfileId
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_item_${profile.id}"),
                                    backgroundColor = if (isActive) LiquidSurfaceElevated else LiquidSurfaceGlass,
                                    glowAccentColor = if (isActive) Color(profile.avatarColorHex) else null,
                                    borderWidth = if (isActive) 1.5.dp else 1.dp,
                                    onClick = {
                                        HapticHelper.triggerImpact(haptic)
                                        onSelectProfile(profile.id)
                                        onDismiss()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color(profile.avatarColorHex).copy(alpha = 0.2f))
                                                .border(1.5.dp, Color(profile.avatarColorHex), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = profile.avatarEmoji, fontSize = 22.sp)
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = profile.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                )
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(LiquidGlowEmerald.copy(alpha = 0.2f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVE",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = LiquidGlowEmerald
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = profile.displayClass,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(profile.avatarColorHex)
                                            )

                                            Text(
                                                text = profile.schoolName,
                                                fontSize = 11.sp,
                                                color = TextMuted,
                                                maxLines = 1
                                            )
                                        }

                                        if (isActive) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color(profile.avatarColorHex),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                HapticHelper.triggerSelection(haptic)
                                isAddingNewProfile = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_add_student_profile"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiquidGlowIndigo.copy(alpha = 0.25f),
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGlowIndigo.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Sibling or Switch Account",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Add Profile Form
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 420.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Student Name") },
                                placeholder = { Text("e.g. Diya Sharma") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_student_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LiquidGlowIndigo,
                                    unfocusedBorderColor = LiquidCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = newEmail,
                                onValueChange = { newEmail = it },
                                label = { Text("Google Account / Student Email") },
                                placeholder = { Text("e.g. diya.sharma@gmail.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_student_email"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LiquidGlowIndigo,
                                    unfocusedBorderColor = LiquidCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        item {
                            Text(
                                text = "Select CBSE Class (1 - 12)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items((1..12).toList()) { cls ->
                                    val isSelected = cls == newClass
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) LiquidGlowIndigo else LiquidSurfaceElevated)
                                            .clickable {
                                                HapticHelper.triggerSelection(haptic)
                                                newClass = cls
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Cl. $cls",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        if (newClass >= 11) {
                            item {
                                Text(
                                    text = "Class 11/12 Academic Stream",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(streams) { stm ->
                                        val isSelected = stm == newStream
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) LiquidGlowCyan else LiquidSurfaceElevated)
                                            .clickable {
                                                HapticHelper.triggerSelection(haptic)
                                                newStream = stm
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = stm,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.Black else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = newSchool,
                                onValueChange = { newSchool = it },
                                label = { Text("School Name") },
                                placeholder = { Text("e.g. Modern School, Vasant Vihar") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_student_school"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LiquidGlowIndigo,
                                    unfocusedBorderColor = LiquidCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        item {
                            Text(
                                text = "Choose Profile Avatar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                emojis.forEach { emoji ->
                                    val isSelected = emoji == selectedEmoji
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) LiquidGlowIndigo.copy(alpha = 0.3f) else LiquidSurfaceElevated)
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) LiquidGlowIndigo else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                HapticHelper.triggerSelection(haptic)
                                                selectedEmoji = emoji
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 18.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    HapticHelper.triggerImpact(haptic)
                                    onAddProfile(
                                        newName,
                                        newEmail,
                                        newClass,
                                        newStream,
                                        newSchool,
                                        selectedEmoji,
                                        selectedColor
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_save_new_profile"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LiquidGlowIndigo,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.School, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Provision CBSE Vault & Activate",
                                    fontSize = 15.sp,
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
