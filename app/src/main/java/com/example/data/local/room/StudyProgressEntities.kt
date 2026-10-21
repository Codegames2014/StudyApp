package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * MODULE 2: StudyProgressEntity & StudyProgressDao
 * Tracks NCERT chapter progress, completion status, revision counts, and timestamps for each student profile.
 */
@Entity(
    tableName = "study_progress",
    indices = [
        Index(value = ["studentProfileId"]),
        Index(value = ["subjectId"]),
        Index(value = ["chapterId"]),
        Index(value = ["studentProfileId", "chapterId"], unique = true)
    ]
)
data class StudyProgressEntity(
    @PrimaryKey
    val id: String, // e.g. "prog_prof_aarav_cl10_sci_ch1"
    val studentProfileId: String,
    val subjectId: String,
    val chapterId: String,
    val isCompleted: Boolean = false,
    val lastStudiedTimestamp: Long = System.currentTimeMillis(),
    val revisionCount: Int = 0
)

data class SubjectMasteryAggregation(
    val subjectId: String,
    val completedCount: Int,
    val totalTracked: Int
)

@Dao
interface StudyProgressDao {

    @Query("SELECT * FROM study_progress WHERE studentProfileId = :profileId")
    fun getAllProgressForProfile(profileId: String): Flow<List<StudyProgressEntity>>

    @Query("SELECT * FROM study_progress WHERE studentProfileId = :profileId AND subjectId = :subjectId")
    fun getProgressForSubject(profileId: String, subjectId: String): Flow<List<StudyProgressEntity>>

    @Query("SELECT * FROM study_progress WHERE studentProfileId = :profileId AND chapterId = :chapterId LIMIT 1")
    fun getProgressForChapter(profileId: String, chapterId: String): Flow<StudyProgressEntity?>

    @Query("SELECT * FROM study_progress WHERE studentProfileId = :profileId AND chapterId = :chapterId LIMIT 1")
    suspend fun getProgressForChapterSync(profileId: String, chapterId: String): StudyProgressEntity?

    @Query("SELECT COUNT(*) FROM study_progress WHERE studentProfileId = :profileId AND subjectId = :subjectId AND isCompleted = 1")
    fun getCompletedCountForSubject(profileId: String, subjectId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_progress WHERE studentProfileId = :profileId AND isCompleted = 1")
    fun getTotalCompletedCount(profileId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: StudyProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<StudyProgressEntity>)

    @Query("UPDATE study_progress SET isCompleted = :isCompleted, lastStudiedTimestamp = :timestamp WHERE studentProfileId = :profileId AND chapterId = :chapterId")
    suspend fun updateCompletion(profileId: String, chapterId: String, isCompleted: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE study_progress SET revisionCount = revisionCount + 1, lastStudiedTimestamp = :timestamp WHERE studentProfileId = :profileId AND chapterId = :chapterId")
    suspend fun incrementRevision(profileId: String, chapterId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM study_progress WHERE studentProfileId = :profileId")
    suspend fun clearProgressForProfile(profileId: String)
}
