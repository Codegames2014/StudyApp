package com.example.data.model

data class StudentProfile(
    val id: String,
    val name: String,
    val email: String,
    val cbseClass: Int, // 1 to 12
    val stream: String, // "General", "Science (PCM)", "Science (PCB)", "Commerce", "Humanities"
    val schoolName: String,
    val avatarEmoji: String,
    val avatarColorHex: Long,
    val studyStreakDays: Int = 1,
    val knowledgePoints: Int = 120,
    val bookmarkedChapterIds: Set<String> = emptySet(),
    val isGoogleLinked: Boolean = true
) {
    val displayClass: String
        get() = "CBSE Class $cbseClass" + if (cbseClass >= 11 && stream.isNotBlank()) " • $stream" else ""
}
