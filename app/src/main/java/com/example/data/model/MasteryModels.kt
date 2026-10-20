package com.example.data.model

data class SkillNode(
    val id: String,
    val chapterId: String,
    val title: String,
    val subjectName: String,
    val cbseClass: Int,
    val nodeTier: Int, // 1: Foundation, 2: Core Concepts, 3: Board Mastery, 4: Exemplar Champion
    val xpReward: Int,
    val isUnlocked: Boolean,
    val isMastered: Boolean,
    val progressPercent: Float, // 0.0 to 1.0
    val connectedNodeIds: List<String> = emptyList(),
    val iconEmoji: String = "⚡"
)

data class CosmeticTheme(
    val id: String,
    val name: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val unlockLevel: Int,
    val isUnlocked: Boolean
)

data class StudentMasteryProgress(
    val profileId: String,
    val currentLevel: Int = 1,
    val currentXp: Int = 120,
    val xpForNextLevel: Int = 300,
    val rankTitle: String = "NCERT Apprentice",
    val activeThemeId: String = "theme_indigo"
)
