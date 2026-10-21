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
import com.example.data.local.room.StudyVaultEntity
import com.example.data.local.room.VaultItemEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RoomVaultsSection(
    repository: StudyFluidRepository,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val activeProfile = repository.activeProfile

    val vaults by repository.getRoomVaultsForActiveProfile().collectAsState(initial = emptyList())

    var showCreateVaultDialog by remember { mutableStateOf(false) }
    var selectedVaultIdForNewItem by remember { mutableStateOf<String?>(null) }

    // Dialog state: New Vault
    var newVaultTitle by remember { mutableStateOf("") }
    var newVaultCategory by remember { mutableStateOf("NCERT Core") }
    var newVaultDesc by remember { mutableStateOf("") }
    var newVaultEmoji by remember { mutableStateOf("📁") }
    var newVaultColor by remember { mutableLongStateOf(0xFF6366F1) }

    // Dialog state: New Item
    var newItemTitle by remember { mutableStateOf("") }
    var newItemContent by remember { mutableStateOf("") }
    var newItemCitation by remember { mutableStateOf("NCERT Class ${activeProfile.cbseClass}") }

    val emojis = listOf("📁", "⚗️", "🔬", "🫀", "📐", "⚡", "📜", "💡")
    val categories = listOf("NCERT Core", "Board Formulae", "Doubt Scans", "Revision Cards", "Practice Questions")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Room Local Vaults",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Categorized notes & textbook archives for ${activeProfile.name}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = {
                    HapticHelper.triggerSelection(haptic)
                    showCreateVaultDialog = true
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidGlowIndigo.copy(alpha = 0.25f),
                    contentColor = TextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_create_room_vault")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (vaults.isEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowAccentColor = LiquidGlowCyan
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📦", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Custom Vaults Yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Create a local Room vault to organize NCERT notes, formula sheets, and solved doubts.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vaults, key = { it.vaultId }) { vault ->
                    RoomVaultCard(
                        vault = vault,
                        repository = repository,
                        onAddItem = {
                            selectedVaultIdForNewItem = vault.vaultId
                        },
                        onDelete = {
                            coroutineScope.launch {
                                repository.deleteRoomVault(vault)
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialog: Create Vault
    if (showCreateVaultDialog) {
        AlertDialog(
            onDismissRequest = { showCreateVaultDialog = false },
            title = { Text("Create Study Vault", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newVaultTitle,
                        onValueChange = { newVaultTitle = it },
                        label = { Text("Vault Title") },
                        placeholder = { Text("e.g. Physics Formula Vault") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category", fontSize = 12.sp, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSel = cat == newVaultCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) LiquidGlowIndigo else LiquidSurfaceElevated)
                                    .clickable { newVaultCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else TextSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newVaultDesc,
                        onValueChange = { newVaultDesc = it },
                        label = { Text("Description") },
                        placeholder = { Text("Key concepts and derivations") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Choose Emoji", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojis.forEach { emoji ->
                            val isSel = emoji == newVaultEmoji
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) LiquidGlowCyan.copy(alpha = 0.3f) else LiquidSurfaceElevated)
                                    .clickable { newVaultEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.triggerImpact(haptic)
                        coroutineScope.launch {
                            repository.createRoomVault(
                                title = newVaultTitle,
                                category = newVaultCategory,
                                description = newVaultDesc,
                                emoji = newVaultEmoji,
                                colorHex = newVaultColor
                            )
                            newVaultTitle = ""
                            newVaultDesc = ""
                            showCreateVaultDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
                ) {
                    Text("Create Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateVaultDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = LiquidSurfaceElevated
        )
    }

    // Dialog: Add Item to Vault
    if (selectedVaultIdForNewItem != null) {
        AlertDialog(
            onDismissRequest = { selectedVaultIdForNewItem = null },
            title = { Text("Add Study Note to Vault", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newItemTitle,
                        onValueChange = { newItemTitle = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("e.g. Sridharacharya Formula") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newItemContent,
                        onValueChange = { newItemContent = it },
                        label = { Text("Note Content / Concept") },
                        placeholder = { Text("Working steps, reaction rules, or notes") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newItemCitation,
                        onValueChange = { newItemCitation = it },
                        label = { Text("NCERT Citation Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.triggerImpact(haptic)
                        val targetVaultId = selectedVaultIdForNewItem ?: return@Button
                        coroutineScope.launch {
                            repository.addRoomVaultItem(
                                vaultId = targetVaultId,
                                title = newItemTitle,
                                content = newItemContent,
                                citationTag = newItemCitation
                            )
                            newItemTitle = ""
                            newItemContent = ""
                            selectedVaultIdForNewItem = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGlowIndigo)
                ) {
                    Text("Save to Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedVaultIdForNewItem = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = LiquidSurfaceElevated
        )
    }
}

@Composable
fun RoomVaultCard(
    vault: StudyVaultEntity,
    repository: StudyFluidRepository,
    onAddItem: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }

    val items by repository.getRoomItemsForVault(vault.vaultId).collectAsState(initial = emptyList())

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("room_vault_card_${vault.vaultId}"),
        glowAccentColor = Color(vault.colorHex),
        onClick = {
            HapticHelper.triggerSelection(haptic)
            isExpanded = !isExpanded
        }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(vault.colorHex).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = vault.iconEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = vault.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${vault.category} • ${items.size} item${if (items.size != 1) "s" else ""}",
                            fontSize = 11.sp,
                            color = Color(vault.colorHex)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            HapticHelper.triggerSelection(haptic)
                            onAddItem()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item", tint = LiquidGlowCyan, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            HapticHelper.triggerImpact(haptic)
                            onDelete()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Vault", tint = LiquidGlowRose, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (vault.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = vault.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = LiquidCardBorder, modifier = Modifier.padding(vertical = 6.dp))

                    if (items.isEmpty()) {
                        Text(
                            text = "No notes in this vault yet. Tap '+' above to add notes.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiquidSurfaceElevated.copy(alpha = 0.6f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = item.content,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                    if (!item.ncertCitationTag.isNullOrBlank()) {
                                        Text(
                                            text = item.ncertCitationTag,
                                            fontSize = 9.sp,
                                            color = LiquidGlowCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        HapticHelper.triggerImpact(haptic)
                                        coroutineScope.launch {
                                            repository.deleteRoomVaultItem(item)
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Item",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
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
