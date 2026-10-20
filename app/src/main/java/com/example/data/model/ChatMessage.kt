package com.example.data.model

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val citationBadges: List<String> = emptyList(),
    val isThinking: Boolean = false,
    val thinkingSteps: List<String> = emptyList(),
    val attachedDiagramName: String? = null
)

enum class MessageSender {
    USER,
    FLUID_TUTOR
}

data class StudentNote(
    val id: String,
    val profileId: String,
    val chapterId: String,
    val title: String,
    val content: String,
    val ncertCitationTag: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
