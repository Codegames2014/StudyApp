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
    val id: String = "mistake_${System.currentTimeMillis()}",
    val studentProfileId: String,
    val chapterId: String = "",
    val subject: String, // "Physics", "Chemistry", "Biology", "Mathematics", "Social Science"
    val topic: String = "",
    val questionText: String,
    val studentAnswer: String = "",
    val correctAnswer: String,
    val explanation: String,
    val mistakeCategory: String = "Conceptual Gap", // "Conceptual Gap", "Calculation Slip", "Formula Recall", "Question Misread"
    val severityLevel: Int = 2, // 1 to 3
    val isMastered: Boolean = false,
    val reviewCount: Int = 0,
    val ncertCitation: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val masteredAt: Long? = null,
    val chapter: String = chapterId,
    val conceptTopic: String = topic,
    val userWrongAnswer: String = studentAnswer,
    val timestamp: Long = createdAt,
    val errorCount: Int = 1
) {
    constructor(
        id: Long = 0L,
        studentProfileId: String,
        subject: String,
        chapter: String,
        conceptTopic: String,
        questionText: String,
        userWrongAnswer: String,
        correctAnswer: String,
        explanation: String,
        timestamp: Long = System.currentTimeMillis(),
        errorCount: Int = 1,
        isMastered: Boolean = false
    ) : this(
        id = if (id == 0L) "mistake_${System.currentTimeMillis()}_${(1000..9999).random()}" else id.toString(),
        studentProfileId = studentProfileId,
        chapterId = chapter,
        subject = subject,
        topic = conceptTopic,
        questionText = questionText,
        studentAnswer = userWrongAnswer,
        correctAnswer = correctAnswer,
        explanation = explanation,
        isMastered = isMastered,
        chapter = chapter,
        conceptTopic = conceptTopic,
        userWrongAnswer = userWrongAnswer,
        timestamp = timestamp,
        errorCount = errorCount
    )
}

data class SubjectErrorSummary(
    val subject: String,
    val totalErrors: Int,
    val unmasteredCount: Int
)

data class ChapterErrorSummary(
    val chapter: String,
    val subject: String,
    val totalErrors: Int,
    val unmasteredCount: Int
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

    @Query("SELECT subject, SUM(errorCount) as totalErrors, SUM(CASE WHEN isMastered = 0 THEN 1 ELSE 0 END) as unmasteredCount FROM mistake_vault WHERE studentProfileId = :profileId GROUP BY subject")
    fun getSubjectWiseErrorAggregation(profileId: String): Flow<List<SubjectErrorSummary>>

    @Query("SELECT subject, SUM(errorCount) as totalErrors, SUM(CASE WHEN isMastered = 0 THEN 1 ELSE 0 END) as unmasteredCount FROM mistake_vault WHERE studentProfileId = :profileId GROUP BY subject")
    fun getSubjectErrorSummaries(profileId: String): Flow<List<SubjectErrorSummary>>

    @Query("SELECT chapter, subject, SUM(errorCount) as totalErrors, SUM(CASE WHEN isMastered = 0 THEN 1 ELSE 0 END) as unmasteredCount FROM mistake_vault WHERE studentProfileId = :profileId AND isMastered = 0 GROUP BY chapter, subject ORDER BY totalErrors DESC LIMIT 3")
    fun getTopWeakChapters(profileId: String): Flow<List<ChapterErrorSummary>>

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

    @Query("UPDATE mistake_vault SET isMastered = :isMastered, masteredAt = :masteredAt WHERE id = :id")
    suspend fun setMasteredStatus(id: String, isMastered: Boolean, masteredAt: Long? = System.currentTimeMillis())

    @Query("UPDATE mistake_vault SET errorCount = errorCount + 1, timestamp = :timestamp WHERE id = :id")
    suspend fun incrementErrorCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE mistake_vault SET reviewCount = reviewCount + 1 WHERE id = :id")
    suspend fun incrementReviewCount(id: String)

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)

    @Query("DELETE FROM mistake_vault WHERE studentProfileId = :profileId")
    suspend fun clearMistakesForProfile(profileId: String)
}
