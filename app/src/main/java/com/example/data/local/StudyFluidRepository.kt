package com.example.data.local

import android.content.Context
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudyFluidRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)

    // --- 1. PROFILES & ISOLATION ---
    private val _profiles = MutableStateFlow<List<StudentProfile>>(defaultProfiles)
    val profiles: StateFlow<List<StudentProfile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow<String>(defaultProfiles[0].id)
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    val activeProfile: StudentProfile
        get() = _profiles.value.find { it.id == _activeProfileId.value } ?: _profiles.value.first()

    // Isolated chat history per profile: profileId -> List<ChatMessage>
    private val _profileChats = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            defaultProfiles[0].id to listOf(
                ChatMessage(
                    id = "msg_init_aarav",
                    sender = MessageSender.FLUID_TUTOR,
                    text = "Namaste Aarav! 🙏 I'm your Socratic FluidTutor for CBSE Class 10. Let's conquer Chemical Reactions, Life Processes, or Quadratic Equations today. You can also scan any textbook equation with the Camera Doubt Scanner!",
                    citationBadges = listOf("NCERT Cl.10 Sci, Ch.1", "NCERT Cl.10 Math, Ch.4")
                )
            ),
            defaultProfiles[1].id to listOf(
                ChatMessage(
                    id = "msg_init_diya",
                    sender = MessageSender.FLUID_TUTOR,
                    text = "Hi Diya! 🌟 Welcome to your Class 7 study vault. We can explore Science, Math puzzles, or prepare for upcoming unit tests. Ask me anything!",
                    citationBadges = listOf("NCERT Cl.7 Foundation")
                )
            )
        )
    )
    val profileChats: StateFlow<Map<String, List<ChatMessage>>> = _profileChats.asStateFlow()

    // Isolated student notes per profile
    private val _notes = MutableStateFlow<List<StudentNote>>(
        listOf(
            StudentNote(
                id = "note_1",
                profileId = defaultProfiles[0].id,
                chapterId = "cl10_sci_ch1",
                title = "Redox Trick: OIL RIG & Zinc Test",
                content = "Oxidation Is Loss of electrons/gain of Oxygen; Reduction Is Gain of electrons/loss of Oxygen. Zn + H2SO4 produces H2 gas which burns with 'pop' sound.",
                ncertCitationTag = "NCERT Cl.10 Sci, Ch.1, p.4",
                isPinned = true
            ),
            StudentNote(
                id = "note_2",
                profileId = defaultProfiles[0].id,
                chapterId = "cl10_sci_ch6",
                title = "Bile Juice Secretion & Emulsification",
                content = "Bile has no digestive enzymes! Its role is solely to create alkaline pH and emulsify large lipid droplets into micro-micelles for lipase.",
                ncertCitationTag = "NCERT Cl.10 Sci, Ch.6, p.100",
                isPinned = false
            )
        )
    )
    val notes: StateFlow<List<StudentNote>> = _notes.asStateFlow()

    // --- 2. OFFLINE CHAPTER CACHE & VECTOR EMBEDDINGS ---
    private val _cachedChapterIds = MutableStateFlow<Set<String>>(setOf("cl10_sci_ch1"))
    val cachedChapterIds: StateFlow<Set<String>> = _cachedChapterIds.asStateFlow()

    // Download progress map: chapterId -> Float (0f to 1f)
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    fun isChapterOffline(chapterId: String): Boolean {
        return _cachedChapterIds.value.contains(chapterId)
    }

    fun downloadChapterForOffline(chapterId: String) {
        if (_cachedChapterIds.value.contains(chapterId)) return
        scope.launch {
            _downloadProgress.update { it + (chapterId to 0.15f) }
            delay(350)
            _downloadProgress.update { it + (chapterId to 0.45f) }
            delay(350)
            _downloadProgress.update { it + (chapterId to 0.85f) }
            delay(300)
            _cachedChapterIds.update { it + chapterId }
            _downloadProgress.update { it - chapterId }
            incrementStreakAndXp(25)
        }
    }

    fun removeChapterFromCache(chapterId: String) {
        _cachedChapterIds.update { it - chapterId }
    }

    // --- 3. GAMIFIED CBSE MASTERY & SKILL TREE ---
    private val _skillNodes = MutableStateFlow<List<SkillNode>>(initialSkillNodes)
    val skillNodes: StateFlow<List<SkillNode>> = _skillNodes.asStateFlow()

    private val _cosmeticThemes = MutableStateFlow<List<CosmeticTheme>>(defaultThemes)
    val cosmeticThemes: StateFlow<List<CosmeticTheme>> = _cosmeticThemes.asStateFlow()

    private val _activeThemeId = MutableStateFlow<String>("theme_indigo")
    val activeThemeId: StateFlow<String> = _activeThemeId.asStateFlow()

    fun completeSkillNode(nodeId: String) {
        _skillNodes.update { list ->
            list.map { node ->
                if (node.id == nodeId) {
                    node.copy(isMastered = true, progressPercent = 1.0f)
                } else node
            }
        }
        // Unlock connected nodes
        val targetNode = _skillNodes.value.find { it.id == nodeId }
        if (targetNode != null) {
            _skillNodes.update { list ->
                list.map { node ->
                    if (targetNode.connectedNodeIds.contains(node.id)) {
                        node.copy(isUnlocked = true)
                    } else node
                }
            }
        }
        incrementStreakAndXp(60)
        checkThemeUnlocks()
    }

    private fun checkThemeUnlocks() {
        val currentXp = activeProfile.knowledgePoints
        _cosmeticThemes.update { list ->
            list.map { theme ->
                if (!theme.isUnlocked && currentXp >= (theme.unlockLevel * 150)) {
                    theme.copy(isUnlocked = true)
                } else theme
            }
        }
    }

    fun setActiveTheme(themeId: String) {
        val target = _cosmeticThemes.value.find { it.id == themeId && it.isUnlocked } ?: return
        _activeThemeId.value = target.id
    }

    // --- 4. SPACED REPETITION PLANNER ---
    private val _revisionTasks = MutableStateFlow<List<RevisionTask>>(initialTasks)
    val revisionTasks: StateFlow<List<RevisionTask>> = _revisionTasks.asStateFlow()

    private val _examCountdowns = MutableStateFlow<List<ExamCountdown>>(
        listOf(
            ExamCountdown("exam_1", "CBSE Science Board Term Finals", "Science", 28, 98),
            ExamCountdown("exam_2", "Mathematics Pre-Board Olympiad", "Mathematics", 45, 95)
        )
    )
    val examCountdowns: StateFlow<List<ExamCountdown>> = _examCountdowns.asStateFlow()

    fun toggleTaskCompletion(taskId: String) {
        _revisionTasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val updatedState = !task.isCompleted
                    if (updatedState) {
                        incrementStreakAndXp(task.xpReward)
                    }
                    task.copy(isCompleted = updatedState)
                } else task
            }
        }
    }

    fun generateSpacedRepetitionPlanForChapter(chapterId: String) {
        val chapter = NcertDatabase.getChapterById(chapterId) ?: NcertDatabase.class10ChemicalReactions
        val newTasks = listOf(
            RevisionTask(
                id = "plan_${System.currentTimeMillis()}_1",
                profileId = _activeProfileId.value,
                title = "Day 1 Active Recall: ${chapter.title}",
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                subjectName = chapter.subjectName,
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.FLASHCARD_SPACED,
                estimatedMinutes = 12,
                xpReward = 30
            ),
            RevisionTask(
                id = "plan_${System.currentTimeMillis()}_2",
                profileId = _activeProfileId.value,
                title = "Day 3 Reinforce: Board Exemplar MCQs",
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                subjectName = chapter.subjectName,
                dueDateOffsetDays = 1,
                taskType = RevisionTaskType.BOARD_MCQ_QUIZ,
                estimatedMinutes = 15,
                xpReward = 45
            ),
            RevisionTask(
                id = "plan_${System.currentTimeMillis()}_3",
                profileId = _activeProfileId.value,
                title = "Day 7 Long-Term Mastery: Formula Derivations",
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                subjectName = chapter.subjectName,
                dueDateOffsetDays = 3,
                taskType = RevisionTaskType.FORMULA_REWRITE,
                estimatedMinutes = 20,
                xpReward = 60
            )
        )
        _revisionTasks.update { newTasks + it }
    }

    fun reorderTasks(fromIndex: Int, toIndex: Int) {
        _revisionTasks.update { list ->
            if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) {
                return@update list
            }
            val mutable = list.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            mutable
        }
    }

    fun moveTaskToDay(taskId: String, newDayOffset: Int) {
        _revisionTasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    task.copy(dueDateOffsetDays = newDayOffset)
                } else task
            }
        }
    }

    fun deleteTask(taskId: String) {
        _revisionTasks.update { list ->
            list.filterNot { it.id == taskId }
        }
    }

    fun generateCustomSpacedSchedule(
        topic: String,
        subject: String,
        examDays: Int,
        dailyMinutes: Int,
        confidence: String
    ) {
        val chapterId = "cl10_sci_ch1"
        val timestamp = System.currentTimeMillis()
        val intensityFactor = when (confidence) {
            "Needs Work" -> 1.3f
            "Intermediate" -> 1.0f
            else -> 0.8f
        }

        val generated = listOf(
            RevisionTask(
                id = "ai_plan_${timestamp}_1",
                profileId = _activeProfileId.value,
                title = "Initial Spaced Study: $topic",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.FLASHCARD_SPACED,
                estimatedMinutes = (dailyMinutes * 0.4 * intensityFactor).toInt().coerceAtLeast(10),
                xpReward = 35
            ),
            RevisionTask(
                id = "ai_plan_${timestamp}_2",
                profileId = _activeProfileId.value,
                title = "NCERT Exemplar & Practice: $topic",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = 1,
                taskType = RevisionTaskType.BOARD_MCQ_QUIZ,
                estimatedMinutes = (dailyMinutes * 0.5 * intensityFactor).toInt().coerceAtLeast(15),
                xpReward = 45
            ),
            RevisionTask(
                id = "ai_plan_${timestamp}_3",
                profileId = _activeProfileId.value,
                title = "SM-2 Interval Recall: Formula Derivation for $topic",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = 3,
                taskType = RevisionTaskType.FORMULA_REWRITE,
                estimatedMinutes = (dailyMinutes * 0.6).toInt().coerceAtLeast(15),
                xpReward = 50
            ),
            RevisionTask(
                id = "ai_plan_${timestamp}_4",
                profileId = _activeProfileId.value,
                title = "Long-Term Retention: Audio Overview & Timed Test",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = if (examDays > 7) 7 else examDays - 1,
                taskType = RevisionTaskType.AUDIO_PODCAST_REVIEW,
                estimatedMinutes = dailyMinutes,
                xpReward = 75
            )
        )

        _revisionTasks.update { generated + it }
        incrementStreakAndXp(30)
    }

    // --- 5. SHARED SIBLING STUDY ROOM & NOTES SYNC ---
    private val _sharedStudyRoom = MutableStateFlow<SharedStudyRoom>(
        SharedStudyRoom(
            roomId = "room_sharma_siblings",
            roomName = "Sharma Sibling CBSE Lounge",
            activeChapterId = "cl10_sci_ch1",
            activeChapterTitle = "Chemical Reactions & Equations",
            sharedScratchpadText = "Aarav (Cl.10) & Diya (Cl.7) Shared Notes:\n• Antoine Lavoisier Law of Mass Conservation: Total mass before = Total mass after.\n• Diya: Remember for Science Unit Test - photosynthesis formula uses chlorophyll and solar photons!\n• Aarav: White AgCl turning grey in sunlight is photolytic decomposition.",
            isCoListeningPodcast = false,
            activePodcastDialogueIndex = 0,
            siblingMessages = listOf(
                SiblingChatMessage(
                    id = "sm_1",
                    senderProfileId = defaultProfiles[0].id,
                    senderName = "Aarav",
                    senderEmoji = "🚀",
                    text = "Diya, I added the photolysis equations to the shared scratchpad! Check it out."
                ),
                SiblingChatMessage(
                    id = "sm_2",
                    senderProfileId = defaultProfiles[1].id,
                    senderName = "Diya",
                    senderEmoji = "✨",
                    text = "Awesome bhaiya! Can we listen to the Deep Dive audio podcast together?"
                )
            )
        )
    )
    val sharedStudyRoom: StateFlow<SharedStudyRoom> = _sharedStudyRoom.asStateFlow()

    fun updateSharedScratchpad(text: String) {
        _sharedStudyRoom.update { it.copy(sharedScratchpadText = text) }
    }

    fun sendSiblingMessage(text: String, isSticker: Boolean = false) {
        val currentProfile = activeProfile
        val newMsg = SiblingChatMessage(
            id = "sm_${System.currentTimeMillis()}",
            senderProfileId = currentProfile.id,
            senderName = currentProfile.name.split(" ").firstOrNull() ?: currentProfile.name,
            senderEmoji = currentProfile.avatarEmoji,
            text = text,
            isStickerOrReaction = isSticker
        )
        _sharedStudyRoom.update {
            it.copy(siblingMessages = it.siblingMessages + newMsg)
        }
    }

    fun toggleCoListening() {
        _sharedStudyRoom.update {
            it.copy(isCoListeningPodcast = !it.isCoListeningPodcast)
        }
    }

    // --- STANDARD PROFILE & APP ACTIONS ---
    fun switchProfile(profileId: String) {
        val target = _profiles.value.find { it.id == profileId } ?: return
        _activeProfileId.value = target.id
    }

    fun addProfile(
        name: String,
        email: String,
        cbseClass: Int,
        stream: String,
        schoolName: String,
        avatarEmoji: String,
        colorHex: Long
    ) {
        val newProfile = StudentProfile(
            id = "prof_${System.currentTimeMillis()}",
            name = name.trim().ifEmpty { "Student" },
            email = email.trim().ifEmpty { "student@cbse.studyfluid.ai" },
            cbseClass = cbseClass.coerceIn(1, 12),
            stream = if (cbseClass >= 11) stream else "General",
            schoolName = schoolName.trim().ifEmpty { "CBSE Affiliated School" },
            avatarEmoji = avatarEmoji,
            avatarColorHex = colorHex,
            studyStreakDays = 1,
            knowledgePoints = 100,
            bookmarkedChapterIds = emptySet()
        )
        _profiles.update { it + newProfile }
        _activeProfileId.value = newProfile.id

        val welcomeMsg = ChatMessage(
            id = "msg_welcome_${newProfile.id}",
            sender = MessageSender.FLUID_TUTOR,
            text = "Welcome aboard, ${newProfile.name}! 🎉 I've calibrated your workspace for CBSE Class ${newProfile.cbseClass}. Use the Camera Doubt Scanner or offline vault anytime!",
            citationBadges = listOf("NCERT Cl.${newProfile.cbseClass}")
        )
        _profileChats.update { current ->
            current + (newProfile.id to listOf(welcomeMsg))
        }
    }

    fun updateActiveProfileDetails(name: String, cbseClass: Int, stream: String, schoolName: String) {
        val currentId = _activeProfileId.value
        _profiles.update { list ->
            list.map { prof ->
                if (prof.id == currentId) {
                    prof.copy(
                        name = name.trim().ifEmpty { prof.name },
                        cbseClass = cbseClass.coerceIn(1, 12),
                        stream = if (cbseClass >= 11) stream else "General",
                        schoolName = schoolName.trim().ifEmpty { prof.schoolName }
                    )
                } else prof
            }
        }
    }

    fun toggleBookmarkChapter(chapterId: String) {
        val currentId = _activeProfileId.value
        _profiles.update { list ->
            list.map { prof ->
                if (prof.id == currentId) {
                    val updated = if (prof.bookmarkedChapterIds.contains(chapterId)) {
                        prof.bookmarkedChapterIds - chapterId
                    } else {
                        prof.bookmarkedChapterIds + chapterId
                    }
                    prof.copy(bookmarkedChapterIds = updated)
                } else prof
            }
        }
    }

    fun addChatMessage(profileId: String, message: ChatMessage) {
        _profileChats.update { current ->
            val existing = current[profileId] ?: emptyList()
            current + (profileId to (existing + message))
        }
    }

    fun clearChat(profileId: String) {
        _profileChats.update { current ->
            current + (profileId to emptyList())
        }
    }

    fun addNote(chapterId: String, title: String, content: String, citationTag: String) {
        val newNote = StudentNote(
            id = "note_${System.currentTimeMillis()}",
            profileId = _activeProfileId.value,
            chapterId = chapterId,
            title = title,
            content = content,
            ncertCitationTag = citationTag
        )
        _notes.update { listOf(newNote) + it }
    }

    fun deleteNote(noteId: String) {
        _notes.update { it.filterNot { n -> n.id == noteId } }
    }

    fun incrementStreakAndXp(xpGain: Int = 25) {
        val currentId = _activeProfileId.value
        _profiles.update { list ->
            list.map { prof ->
                if (prof.id == currentId) {
                    prof.copy(
                        knowledgePoints = prof.knowledgePoints + xpGain
                    )
                } else prof
            }
        }
        checkThemeUnlocks()
    }

    companion object {
        val defaultProfiles = listOf(
            StudentProfile(
                id = "prof_aarav_cl10",
                name = "Aarav Sharma",
                email = "aarav.sharma24@gmail.com",
                cbseClass = 10,
                stream = "General",
                schoolName = "Delhi Public School, R.K. Puram",
                avatarEmoji = "🚀",
                avatarColorHex = 0xFF6366F1,
                studyStreakDays = 7,
                knowledgePoints = 640,
                bookmarkedChapterIds = setOf("cl10_sci_ch1", "cl10_sci_ch6")
            ),
            StudentProfile(
                id = "prof_diya_cl7",
                name = "Diya Sharma (Sibling)",
                email = "diya.sharma29@gmail.com",
                cbseClass = 7,
                stream = "General",
                schoolName = "Modern School, Vasant Vihar",
                avatarEmoji = "✨",
                avatarColorHex = 0xFF06B6D4,
                studyStreakDays = 4,
                knowledgePoints = 320,
                bookmarkedChapterIds = emptySet()
            )
        )

        val defaultThemes = listOf(
            CosmeticTheme("theme_indigo", "Cyber Indigo", 0xFF6366F1, 0xFF06B6D4, 1, true),
            CosmeticTheme("theme_emerald", "Aurora Emerald", 0xFF10B981, 0xFF06B6D4, 2, true),
            CosmeticTheme("theme_amber", "Solar Flare Amber", 0xFFF59E0B, 0xFFEF4444, 3, true),
            CosmeticTheme("theme_violet", "Nebula Violet", 0xFF8B5CF6, 0xFFEC4899, 4, false),
            CosmeticTheme("theme_rose", "Quantum Rose", 0xFFF43F5E, 0xFF8B5CF6, 5, false)
        )

        val initialSkillNodes = listOf(
            SkillNode(
                id = "node_chem_1",
                chapterId = "cl10_sci_ch1",
                title = "Chemical Equations & Mass Conservation",
                subjectName = "Chemistry",
                cbseClass = 10,
                nodeTier = 1,
                xpReward = 50,
                isUnlocked = true,
                isMastered = true,
                progressPercent = 1.0f,
                connectedNodeIds = listOf("node_chem_2", "node_bio_1"),
                iconEmoji = "⚗️"
            ),
            SkillNode(
                id = "node_chem_2",
                chapterId = "cl10_sci_ch1",
                title = "Redox & Whitewash Reactions",
                subjectName = "Chemistry",
                cbseClass = 10,
                nodeTier = 2,
                xpReward = 75,
                isUnlocked = true,
                isMastered = false,
                progressPercent = 0.65f,
                connectedNodeIds = listOf("node_chem_3"),
                iconEmoji = "🔥"
            ),
            SkillNode(
                id = "node_chem_3",
                chapterId = "cl10_sci_ch1",
                title = "Exemplar Board Mastery (Corrosion/Rancidity)",
                subjectName = "Chemistry",
                cbseClass = 10,
                nodeTier = 3,
                xpReward = 100,
                isUnlocked = false,
                isMastered = false,
                progressPercent = 0.0f,
                connectedNodeIds = emptyList(),
                iconEmoji = "👑"
            ),
            SkillNode(
                id = "node_bio_1",
                chapterId = "cl10_sci_ch6",
                title = "Photosynthesis & Stomata Dynamics",
                subjectName = "Biology",
                cbseClass = 10,
                nodeTier = 1,
                xpReward = 50,
                isUnlocked = true,
                isMastered = true,
                progressPercent = 1.0f,
                connectedNodeIds = listOf("node_bio_2"),
                iconEmoji = "🌿"
            ),
            SkillNode(
                id = "node_bio_2",
                chapterId = "cl10_sci_ch6",
                title = "Double Circulation & Nephron Ultrafiltration",
                subjectName = "Biology",
                cbseClass = 10,
                nodeTier = 2,
                xpReward = 80,
                isUnlocked = true,
                isMastered = false,
                progressPercent = 0.4f,
                connectedNodeIds = listOf("node_opt_1"),
                iconEmoji = "🫀"
            ),
            SkillNode(
                id = "node_opt_1",
                chapterId = "cl10_sci_ch10",
                title = "Geometric Optics & Snell's Law",
                subjectName = "Physics",
                cbseClass = 10,
                nodeTier = 2,
                xpReward = 90,
                isUnlocked = false,
                isMastered = false,
                progressPercent = 0.0f,
                connectedNodeIds = listOf("node_quad_1"),
                iconEmoji = "🔦"
            ),
            SkillNode(
                id = "node_quad_1",
                chapterId = "cl10_math_ch4",
                title = "Quadratic Discriminant & Sridharacharya",
                subjectName = "Mathematics",
                cbseClass = 10,
                nodeTier = 3,
                xpReward = 120,
                isUnlocked = false,
                isMastered = false,
                progressPercent = 0.0f,
                connectedNodeIds = emptyList(),
                iconEmoji = "📐"
            )
        )

        val initialTasks = listOf(
            RevisionTask(
                id = "task_today_1",
                profileId = "prof_aarav_cl10",
                title = "Daily Spaced Recall: Chemical Reactions (Ch.1)",
                chapterId = "cl10_sci_ch1",
                chapterTitle = "Chemical Reactions and Equations",
                subjectName = "Chemistry",
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.FLASHCARD_SPACED,
                isCompleted = false,
                estimatedMinutes = 10,
                xpReward = 25
            ),
            RevisionTask(
                id = "task_today_2",
                profileId = "prof_aarav_cl10",
                title = "Nephron Diagram & Double Circulation Quiz",
                chapterId = "cl10_sci_ch6",
                chapterTitle = "Life Processes",
                subjectName = "Biology",
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.BOARD_MCQ_QUIZ,
                isCompleted = true,
                estimatedMinutes = 15,
                xpReward = 35
            ),
            RevisionTask(
                id = "task_tomorrow_1",
                profileId = "prof_aarav_cl10",
                title = "Convex Lens Sign Convention Derivation",
                chapterId = "cl10_sci_ch10",
                chapterTitle = "Light – Reflection & Refraction",
                subjectName = "Physics",
                dueDateOffsetDays = 1,
                taskType = RevisionTaskType.FORMULA_REWRITE,
                isCompleted = false,
                estimatedMinutes = 20,
                xpReward = 40
            ),
            RevisionTask(
                id = "task_spaced_1",
                profileId = "prof_aarav_cl10",
                title = "Listen to Two-Host Deep Dive on Quadratic Equations",
                chapterId = "cl10_math_ch4",
                chapterTitle = "Quadratic Equations",
                subjectName = "Mathematics",
                dueDateOffsetDays = 3,
                taskType = RevisionTaskType.AUDIO_PODCAST_REVIEW,
                isCompleted = false,
                estimatedMinutes = 16,
                xpReward = 30
            )
        )
    }
}
