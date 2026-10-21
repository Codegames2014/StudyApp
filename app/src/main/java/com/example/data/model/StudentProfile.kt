package com.example.data.model

data class StudentProfile(
    val id: String,
    val name: String,
    val email: String,
    val cbseClass: Int, // 1 to 12
    val stream: String = "General", // "General", "Science (PCM)", "Science (PCB)", "Commerce", "Humanities"
    val schoolName: String = "CBSE Affiliated School",
    val avatarEmoji: String = "🎓",
    val avatarColorHex: Long = 0xFF6366F1,
    val studyStreakDays: Int = 1,
    val knowledgePoints: Int = 120,
    val bookmarkedChapterIds: Set<String> = emptySet(),
    val isGoogleLinked: Boolean = true,
    val password: String = "study123",
    val firebaseUid: String? = null,
    val board: String = "CBSE", // "CBSE", "ICSE", "State Board"
    val state: String = "Delhi", // "Karnataka", "Maharashtra", "Delhi", etc.
    val city: String = "New Delhi", // "Bidar", "Bengaluru", "Pune", etc.
    val studyRoadmapSummary: String = "",
    val recommendedChapters: List<String> = emptyList(),
    val dailyScheduleJson: String = "",
    val isOnboardingCompleted: Boolean = false
) {
    val displayClass: String
        get() = "$board Class $cbseClass" + if (cbseClass >= 11 && stream.isNotBlank()) " • $stream" else ""
}

