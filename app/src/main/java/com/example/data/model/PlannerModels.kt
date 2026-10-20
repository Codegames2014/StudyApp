package com.example.data.model

data class RevisionTask(
    val id: String,
    val profileId: String,
    val title: String,
    val chapterId: String,
    val chapterTitle: String,
    val subjectName: String,
    val dueDateOffsetDays: Int, // 0 = Today, 1 = Tomorrow, 3 = In 3 Days (spaced repetition)
    val taskType: RevisionTaskType,
    val isCompleted: Boolean = false,
    val estimatedMinutes: Int = 15,
    val xpReward: Int = 30
)

enum class RevisionTaskType(val label: String, val emoji: String) {
    FLASHCARD_SPACED("Flashcard Active Recall", "🗂️"),
    BOARD_MCQ_QUIZ("NCERT MCQ Practice", "🎯"),
    FORMULA_REWRITE("Formula Derivation Sheet", "📐"),
    AUDIO_PODCAST_REVIEW("Deep Dive Podcast Re-listen", "🎧"),
    EXEMPLAR_PROBLEMS("Exemplar High-Yield Questions", "🏆")
}

data class ExamCountdown(
    val id: String,
    val examName: String,
    val subject: String,
    val daysRemaining: Int,
    val targetScorePercentage: Int = 95
)
