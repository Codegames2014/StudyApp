package com.example.data.model

data class SharedStudyRoom(
    val roomId: String,
    val roomName: String,
    val activeChapterId: String = "cl10_sci_ch1",
    val activeChapterTitle: String = "Chemical Reactions & Equations",
    val sharedScratchpadText: String = "",
    val isCoListeningPodcast: Boolean = false,
    val activePodcastDialogueIndex: Int = 0,
    val siblingMessages: List<SiblingChatMessage> = emptyList()
)

data class SiblingChatMessage(
    val id: String,
    val senderProfileId: String,
    val senderName: String,
    val senderEmoji: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStickerOrReaction: Boolean = false
)
