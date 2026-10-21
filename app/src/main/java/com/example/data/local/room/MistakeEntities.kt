package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "mistake_vault",
    indices = [
        Index(value = ["studentProfileId"]),
        Index(value = ["subject"]),
        Index(value = ["isMastered"])
    ]
)
data class MistakeEntity(
    @PrimaryKey
    val id: String,
    val studentProfileId: String,
    val chapterId: String,
    val subject: String, // "Physics", "Chemistry", "Biology", "Mathematics", "Social Science"
    val topic: String,
    val questionText: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val mistakeCategory: String, // "Conceptual Gap", "Calculation Slip", "Formula Recall", "Question Misread"
    val severityLevel: Int = 2, // 1 to 3
    val isMastered: Boolean = false,
    val reviewCount: Int = 0,
    val ncertCitation: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val masteredAt: Long? = null
)

@Dao
interface MistakeDao {

    @Query("SELECT * FROM mistake_vault WHERE studentProfileId = :profileId ORDER BY isMastered ASC, createdAt DESC")
    fun getAllMistakesForProfile(profileId: String): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_vault WHERE studentProfileId = :profileId AND isMastered = 0 ORDER BY severityLevel DESC, createdAt DESC")
    fun getUnmasteredMistakesForProfile(profileId: String): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_vault WHERE studentProfileId = :profileId AND subject = :subject ORDER BY isMastered ASC, createdAt DESC")
    fun getMistakesBySubject(profileId: String, subject: String): Flow<List<MistakeEntity>>

    @Query("SELECT COUNT(*) FROM mistake_vault WHERE studentProfileId = :profileId AND isMastered = 0")
    fun getUnmasteredCount(profileId: String): Flow<Int>

    @Query("SELECT * FROM mistake_vault WHERE id = :id LIMIT 1")
    suspend fun getMistakeById(id: String): MistakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mistakes: List<MistakeEntity>)

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Query("UPDATE mistake_vault SET isMastered = 1, masteredAt = :timestamp, reviewCount = reviewCount + 1 WHERE id = :id")
    suspend fun markMistakeMastered(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE mistake_vault SET reviewCount = reviewCount + 1 WHERE id = :id")
    suspend fun incrementReviewCount(id: String)

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)

    @Query("DELETE FROM mistake_vault WHERE studentProfileId = :profileId")
    suspend fun clearMistakesForProfile(profileId: String)
}
