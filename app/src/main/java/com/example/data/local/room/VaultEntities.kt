package com.example.data.local.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_vaults",
    indices = [Index(value = ["studentProfileId"])]
)
data class StudyVaultEntity(
    @PrimaryKey
    val vaultId: String,
    val studentProfileId: String,
    val title: String,
    val category: String, // "NCERT Science", "Maths Olympiad", "Doubt Scans", "Revision Cards"
    val description: String,
    val iconEmoji: String,
    val colorHex: Long,
    val itemCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "vault_items",
    indices = [
        Index(value = ["vaultId"]),
        Index(value = ["studentProfileId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = StudyVaultEntity::class,
            parentColumns = ["vaultId"],
            childColumns = ["vaultId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VaultItemEntity(
    @PrimaryKey
    val itemId: String,
    val vaultId: String,
    val studentProfileId: String,
    val chapterId: String? = null,
    val title: String,
    val content: String,
    val itemType: String, // "NOTE", "PDF_CHAPTER", "IMAGE_DOUBT", "AI_PODCAST_SUMMARY", "FLASHCARD"
    val fileUriOrPath: String? = null,
    val ncertCitationTag: String? = null,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
