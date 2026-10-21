package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "student_profiles",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["board"]),
        Index(value = ["state"])
    ]
)
data class StudentProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val gradeClass: Int, // 1 to 12
    val board: String = "CBSE", // "CBSE", "ICSE", "State Board"
    val state: String = "Delhi", // "Karnataka", "Maharashtra", "Delhi", etc.
    val city: String = "New Delhi", // "Bidar", "Bengaluru", "Pune", etc.
    val stream: String = "General", // "General", "Science (PCM)", "Science (PCB)", "Commerce", "Humanities"
    val schoolName: String = "CBSE Affiliated School",
    val avatarEmoji: String = "🎓",
    val avatarColorHex: Long = 0xFF6366F1,
    val studyStreakDays: Int = 1,
    val knowledgePoints: Int = 120,
    val studyRoadmapSummary: String = "",
    val recommendedChaptersJson: String = "",
    val dailyScheduleJson: String = "",
    val isOnboardingCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface StudentProfileDao {

    @Query("SELECT * FROM student_profiles ORDER BY createdAt ASC")
    fun getAllProfilesFlow(): Flow<List<StudentProfileEntity>>

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): StudentProfileEntity?

    @Query("SELECT * FROM student_profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): StudentProfileEntity?

    @Query("SELECT COUNT(*) FROM student_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudentProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<StudentProfileEntity>)

    @Update
    suspend fun updateProfile(profile: StudentProfileEntity)

    @Query("UPDATE student_profiles SET name = :name, gradeClass = :grade, stream = :stream, schoolName = :school WHERE id = :id")
    suspend fun updateBasicDetails(id: String, name: String, grade: Int, stream: String, school: String)

    @Query("UPDATE student_profiles SET name = :name, board = :board, state = :state, city = :city, gradeClass = :grade, stream = :stream, schoolName = :school, studyRoadmapSummary = :roadmap, recommendedChaptersJson = :chapters, dailyScheduleJson = :schedule, isOnboardingCompleted = 1 WHERE id = :id")
    suspend fun updateOnboardingSurvey(
        id: String,
        name: String,
        board: String,
        state: String,
        city: String,
        grade: Int,
        stream: String,
        school: String,
        roadmap: String,
        chapters: String,
        schedule: String
    )

    @Query("UPDATE student_profiles SET isOnboardingCompleted = 1 WHERE id = :id")
    suspend fun markOnboardingCompleted(id: String)

    @Delete
    suspend fun deleteProfile(profile: StudentProfileEntity)

    @Query("DELETE FROM student_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)
}
